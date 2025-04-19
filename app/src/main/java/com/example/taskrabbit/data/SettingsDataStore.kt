package com.example.taskrabbit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.taskrabbit.ui.theme.ThemeSettings
import com.example.taskrabbit.ui.theme.BackgroundChoice

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    // Define keys for the preferences
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val backgroundChoiceKey = stringPreferencesKey("background_choice")

    // Function to save theme settings
    suspend fun saveThemeSettings(themeSettings: ThemeSettings) {
        dataStore.edit { preferences ->
            preferences[darkModeKey] = themeSettings.darkModeEnabled
            preferences[backgroundChoiceKey] = themeSettings.backgroundChoice.name
        }
    }

    // Function to get theme settings
    fun getThemeSettings(): Flow<ThemeSettings> {
        return dataStore.data
            .map { preferences ->
                val darkModeEnabled = preferences[darkModeKey] ?: false
                val backgroundChoiceName = preferences[backgroundChoiceKey] ?: BackgroundChoice.WHITE.name
                val backgroundChoice = BackgroundChoice.valueOf(backgroundChoiceName)

                ThemeSettings(
                    darkModeEnabled = darkModeEnabled,
                    backgroundChoice = backgroundChoice
                )
            }
    }
}