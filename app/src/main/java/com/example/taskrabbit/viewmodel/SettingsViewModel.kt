package com.example.taskrabbit.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.taskrabbit.ui.theme.AppState
import com.example.taskrabbit.ui.theme.BackgroundChoice
import android.util.Log // Import Log

class SettingsViewModel : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    fun setBackgroundChoice(backgroundChoice: BackgroundChoice) {
        _appState.value = _appState.value.copy(backgroundChoice = backgroundChoice)
        Log.d("SettingsViewModel", "Background choice set to: $backgroundChoice") // Add logging
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