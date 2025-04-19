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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()
    private val dataStore = TaskRabbitApplication.getDataStore()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val storedBackgroundChoice = dataStore.data
                .map { preferences ->
                    preferences[BACKGROUND_CHOICE_KEY] ?: BackgroundChoice.WHITE.name
                }.first()
            val storedDarkMode = dataStore.data
                .map { preferences ->
                    preferences[DARK_MODE_KEY] ?: false
                }.first()

            _themeSettings.value = ThemeSettings(
                backgroundChoice = BackgroundChoice.valueOf(storedBackgroundChoice),
                darkModeEnabled = storedDarkMode
            )
            _appState.value = _appState.value.copy(
                backgroundChoice = BackgroundChoice.valueOf(storedBackgroundChoice),
                darkModeEnabled = storedDarkMode
            )
        }
    }

    fun updateThemeSettings(themeSettings: ThemeSettings) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BACKGROUND_CHOICE_KEY] = themeSettings.backgroundChoice.name
                preferences[DARK_MODE_KEY] = themeSettings.darkModeEnabled
            }
            _themeSettings.value = themeSettings
            _appState.value = _appState.value.copy(
                backgroundChoice = themeSettings.backgroundChoice,
                darkModeEnabled = themeSettings.darkModeEnabled
            )
            Log.d("SettingsViewModel", "Theme settings updated: $themeSettings")
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _appState.value = _appState.value.copy(notificationsEnabled = enabled)
    }

    fun setLanguage(language: String) {
        _appState.value = _appState.value.copy(language = language)
    }

    companion object {
        private val BACKGROUND_CHOICE_KEY = stringPreferencesKey("background_choice")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(application) as T
                }
            }
        }
    }
}