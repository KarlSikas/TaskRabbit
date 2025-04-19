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

class SettingsViewModel() : ViewModel() {
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
                .map {preferences ->
                    preferences[DARK_MODE_KEY] ?: false
                }.first()

            _themeSettings.value = ThemeSettings(backgroundChoice = BackgroundChoice.valueOf(storedBackgroundChoice), darkModeEnabled = storedDarkMode)
            _appState.value = _appState.value.copy(backgroundChoice = BackgroundChoice.valueOf(storedBackgroundChoice), darkModeEnabled = storedDarkMode)
        }
    }

    fun setBackgroundChoice(backgroundChoice: BackgroundChoice) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BACKGROUND_CHOICE_KEY] = backgroundChoice.name
            }
            _themeSettings.value = _themeSettings.value.copy(backgroundChoice = backgroundChoice)
            _appState.value = _appState.value.copy(backgroundChoice = backgroundChoice)
        }
        Log.d("SettingsViewModel", "Background choice set to: $backgroundChoice")
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _appState.value = _appState.value.copy(notificationsEnabled = enabled)
    }

    fun setLanguage(language: String) {
        _appState.value = _appState.value.copy(language = language)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = enabled
            }
            _themeSettings.value = _themeSettings.value.copy(darkModeEnabled = enabled)
            _appState.value = _appState.value.copy(darkModeEnabled = enabled)
        }
    }

    companion object {
        private val BACKGROUND_CHOICE_KEY = stringPreferencesKey("background_choice")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }
}