package com.example.taskrabbit.ui.theme

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.taskrabbit.ui.theme.BackgroundChoice

data class ThemeSettingsSerializer(
    val darkModeEnabled: Boolean = false,
    val backgroundChoice: BackgroundChoice = BackgroundChoice.WHITE // Default background
)