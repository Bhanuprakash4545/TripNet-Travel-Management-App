package com.example.travelsapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val EMAIL = stringPreferencesKey("email")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map {
        preferences ->
        val phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER] ?: ""
        val email = preferences[PreferencesKeys.EMAIL] ?: ""
        UserPreferences(phoneNumber, email)
    }

    suspend fun saveUserPreferences(phoneNumber: String, email: String) {
        dataStore.edit {
            preferences ->
            preferences[PreferencesKeys.PHONE_NUMBER] = phoneNumber
            preferences[PreferencesKeys.EMAIL] = email
        }
    }
}

data class UserPreferences(val phoneNumber: String, val email: String)
