package com.example.travelsapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DriverViewModel(private val driverDao: DriverDao) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    private val _addSuccessFlow = MutableSharedFlow<Unit>()
    val addSuccessFlow: SharedFlow<Unit> = _addSuccessFlow.asSharedFlow()

    val drivers = driverDao.getAllDrivers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun addDriver(driver: Driver) {
        viewModelScope.launch {
            val existing = driverDao.getDriverByPhone(driver.phoneNumber)
            if (existing == null) {
                driverDao.insert(driver)
                _addSuccessFlow.emit(Unit)
            } else {
                _errorFlow.emit("Driver with this phone number already exists!")
            }
        }
    }

    fun updateDriver(driver: Driver) {
        viewModelScope.launch {
            driverDao.update(driver)
        }
    }

    fun deleteDriver(driver: Driver) {
        viewModelScope.launch {
            driverDao.delete(driver)
        }
    }

    fun resetDriverDevice(driver: Driver) {
        viewModelScope.launch {
            driverDao.update(driver.copy(deviceId = null))
        }
    }
}

class DriverViewModelFactory(private val driverDao: DriverDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DriverViewModel(driverDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
