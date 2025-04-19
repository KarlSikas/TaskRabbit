package com.example.taskrabbit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.taskrabbit.ui.theme.AppState
import com.example.taskrabbit.ui.theme.BackgroundChoice
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.example.taskrabbit.ui.theme.ThemeSettings // Import ThemeSettings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.example.taskrabbit.TaskRabbitApplication
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.taskrabbit.ui.theme.AppThemeSettings // Import AppThemeSettings

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // ViewModel's internal state (might be partly redundant if AppThemeSettings is the source of truth for UI)
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    private val _themeSettings = MutableStateFlow(ThemeSettings()) // Internal copy
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private val dataStore = TaskRabbitApplication.getDataStore()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load from DataStore (using .first() is okay for initial load)
            val storedBackgroundChoiceName = dataStore.data
                .map { preferences ->
                    preferences[BACKGROUND_CHOICE_KEY] ?: BackgroundChoice.WHITE.name
                }.first()
            val storedDarkMode = dataStore.data
                .map { preferences ->
                    preferences[DARK_MODE_KEY] ?: false
                }.first()

            // Safely convert name to enum, defaulting to WHITE if invalid
            val backgroundChoice = try {
                BackgroundChoice.valueOf(storedBackgroundChoiceName)
            } catch (e: IllegalArgumentException) {
                Log.w("SettingsViewModel", "Invalid background choice '$storedBackgroundChoiceName' found in DataStore. Defaulting to WHITE.")
                BackgroundChoice.WHITE
            }

            val loadedSettings = ThemeSettings(
                backgroundChoice = backgroundChoice,
                darkModeEnabled = storedDarkMode
            )

            // Update ViewModel's internal state
            _themeSettings.value = loadedSettings
            _appState.update { it.copy(
                backgroundChoice = loadedSettings.backgroundChoice,
                darkModeEnabled = loadedSettings.darkModeEnabled
            )}

            // --- Add this line ---
            // Update the global AppThemeSettings state so UI components collecting it get the initial value
            AppThemeSettings.updateThemeSettings(loadedSettings)
            // --- End Add ---

            Log.d("SettingsViewModel", "Initial settings loaded: $loadedSettings")
        }
    }

    fun updateThemeSettings(newThemeSettings: ThemeSettings) {
        viewModelScope.launch {
            // 1. Save to DataStore
            dataStore.edit { preferences ->
                preferences[BACKGROUND_CHOICE_KEY] = newThemeSettings.backgroundChoice.name
                preferences[DARK_MODE_KEY] = newThemeSettings.darkModeEnabled
            }

            // 2. Update ViewModel's internal state (optional if AppThemeSettings is the single source of truth)
            _themeSettings.value = newThemeSettings
            _appState.update { it.copy(
                backgroundChoice = newThemeSettings.backgroundChoice,
                darkModeEnabled = newThemeSettings.darkModeEnabled
            )}

            // --- Add this line ---
            // 3. Update the global AppThemeSettings state flow
            // This is what SettingsScreen is collecting, so this triggers the immediate UI update.
            AppThemeSettings.updateThemeSettings(newThemeSettings)
            // --- End Add ---

            Log.d("SettingsViewModel", "Theme settings updated and saved: $newThemeSettings")
        }
    }

    // Other functions remain the same
    fun setNotificationsEnabled(enabled: Boolean) {
        _appState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setLanguage(language: String) {
        _appState.update { it.copy(language = language) }
    }

    companion object {
        private val BACKGROUND_CHOICE_KEY = stringPreferencesKey("background_choice")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

        // Factory remains the same
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}