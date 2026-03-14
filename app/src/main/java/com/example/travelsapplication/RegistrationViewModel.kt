package com.example.travelsapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistrationViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    fun saveRegistration(phoneNumber: String, email: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserPreferences(phoneNumber, email)
        }
    }

    class RegistrationViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegistrationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
