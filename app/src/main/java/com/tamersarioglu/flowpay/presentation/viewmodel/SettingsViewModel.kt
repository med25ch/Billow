package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.worker.WorkManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val workManagerHelper: WorkManagerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                    darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
                    reminderDays = preferences[REMINDER_DAYS] ?: 3
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[NOTIFICATIONS_ENABLED] = enabled
            }

            if (enabled) {
                workManagerHelper.schedulePeriodicReminders()
            } else {
                workManagerHelper.cancelReminders()
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_MODE_ENABLED] = enabled
            }
        }
    }

    fun updateReminderDays(days: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[REMINDER_DAYS] = days
            }
        }
    }

    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val REMINDER_DAYS = intPreferencesKey("reminder_days")
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val reminderDays: Int = 3
)