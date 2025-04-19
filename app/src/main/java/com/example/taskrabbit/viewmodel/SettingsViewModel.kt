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

class SettingsViewModel : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    fun setBackgroundChoice(backgroundChoice: BackgroundChoice) {
        viewModelScope.launch {
            _themeSettings.update {
                it.copy(backgroundChoice = backgroundChoice)
            }
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
        _appState.value = _appState.value.copy(darkModeEnabled = enabled)
    }
}