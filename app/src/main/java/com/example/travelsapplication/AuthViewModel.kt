package com.example.travelsapplication

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Initial : AuthResult()
    object OtpSent : AuthResult()
    object Loading : AuthResult()
    object LoggedOut : AuthResult()
}

class AuthViewModel(
    private val userDao: UserDao,
    private val driverDao: DriverDao
) : ViewModel() {

    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Initial)
    val authResult: StateFlow<AuthResult> = _authResult.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var verificationId: String? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = userDao.getFirstUser()
                _currentUser.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user", e)
            }
        }
    }

    private fun sanitizePhoneNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.replace(Regex("[^0-9+]"), "")
        return if (cleaned.startsWith("+")) cleaned else "+91$cleaned"
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        val cleanPhone = sanitizePhoneNumber(phoneNumber)
        Log.d("AuthViewModel", "sendOtp called for $cleanPhone")
        _authResult.value = AuthResult.Loading
        
        try {
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(cleanPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error in verifyPhoneNumber", e)
            _authResult.value = AuthResult.Error(e.message ?: "Failed to initiate verification")
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("AuthViewModel", "onVerificationCompleted")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            Log.w("AuthViewModel", "onVerificationFailed", e)
            _authResult.value = AuthResult.Error(e.message ?: "Verification failed")
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("AuthViewModel", "onCodeSent: $id")
            this@AuthViewModel.verificationId = id
            _authResult.value = AuthResult.OtpSent
        }
    }

    fun verifyOtp(otp: String) {
        _authResult.value = AuthResult.Loading
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, otp)
            signInWithPhoneAuthCredential(credential)
        } ?: run {
            _authResult.value = AuthResult.Error("Session expired. Please request a new OTP.")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authResult.value = AuthResult.Success
                } else {
                    _authResult.value = AuthResult.Error(task.exception?.message ?: "Sign in failed")
                }
            }
    }

    fun onLogin(phoneNumber: String, activity: Activity) {
        if (phoneNumber.isBlank()) {
            _authResult.value = AuthResult.Error("Please enter phone number")
            return
        }
        viewModelScope.launch {
            val cleanPhone = sanitizePhoneNumber(phoneNumber)
            val user = userDao.getUserByPhoneNumber(cleanPhone)
            if (user == null) {
                _authResult.value = AuthResult.Error("Account does not exist. Please register.")
            } else {
                _currentUser.value = user
                sendOtp(cleanPhone, activity)
            }
        }
    }

    fun onDriverLogin(phoneNumber: String, pin: String, currentDeviceId: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _authResult.value = AuthResult.Error("Please enter phone and PIN")
            return
        }
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            val cleanPhone = sanitizePhoneNumber(phoneNumber)
            val driver = driverDao.getDriverByPhoneAndPin(cleanPhone, pin)
            if (driver != null) {
                // Device Binding Check
                if (driver.deviceId == null) {
                    // First login, bind this device
                    driverDao.update(driver.copy(deviceId = currentDeviceId))
                    completeDriverLogin(driver)
                } else if (driver.deviceId == currentDeviceId) {
                    // Correct device
                    completeDriverLogin(driver)
                } else {
                    // Wrong device attempt
                    _authResult.value = AuthResult.Error("Account locked to another device. Contact Admin.")
                }
            } else {
                _authResult.value = AuthResult.Error("Invalid Phone or PIN")
            }
        }
    }

    private fun completeDriverLogin(driver: Driver) {
        val user = User(
            phoneNumber = driver.phoneNumber,
            travelsName = driver.name,
            state = "",
            city = "",
            role = "Driver"
        )
        _currentUser.value = user
        _authResult.value = AuthResult.Success
    }

    fun onRegister(phoneNumber: String, travelsName: String, state: String, city: String, activity: Activity) {
        if (phoneNumber.isBlank() || travelsName.isBlank() || state.isBlank() || city.isBlank()) {
            _authResult.value = AuthResult.Error("Please fill all fields")
            return
        }
        viewModelScope.launch {
            val cleanPhone = sanitizePhoneNumber(phoneNumber)
            val existingUser = userDao.getUserByPhoneNumber(cleanPhone)
            if (existingUser != null) {
                _authResult.value = AuthResult.Error("User already exists. Please login.")
            } else {
                val user = User(phoneNumber = cleanPhone, travelsName = travelsName, state = state, city = city)
                userDao.insert(user)
                _currentUser.value = user
                sendOtp(cleanPhone, activity)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                _currentUser.value = null
                _authResult.value = AuthResult.LoggedOut
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging out", e)
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                val existingUser = userDao.getUserByPhoneNumber(user.phoneNumber)
                if (existingUser == null) {
                    userDao.insert(user)
                } else {
                    userDao.update(user)
                }
                _currentUser.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating user", e)
            }
        }
    }

    fun resetAuthResult() {
        _authResult.value = AuthResult.Initial
    }
}

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val driverDao: DriverDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, driverDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
