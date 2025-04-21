package com.example.taskrabbit.viewmodel

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope // Keep for loadSettings
import com.example.taskrabbit.LANGUAGE_KEY // Import key from Application file
import com.example.taskrabbit.TaskRabbitApplication
import com.example.taskrabbit.ui.theme.AppThemeSettings
import com.example.taskrabbit.ui.theme.AppState
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // Keep for loadSettings
import java.lang.IllegalArgumentException
// --- Add Context import ---
import android.content.Context
// --- End ---


open class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Existing State
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    // DataStore instance
    private val dataStore: DataStore<Preferences> by lazy {
        getApplication<TaskRabbitApplication>().dataStore
    }

    // Language Preference State
    private val _currentLanguagePreference = MutableStateFlow("en") // Default to 'en'
    open val currentLanguagePreference: StateFlow<String> = _currentLanguagePreference.asStateFlow()

    // Keys (Theme keys remain here, Language key is now in Application for MainActivity access)
    companion object {
        private val BACKGROUND_CHOICE_KEY = stringPreferencesKey("background_choice")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        // LANGUAGE_KEY is now defined in TaskRabbitApplication.kt

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

    init {
        Log.d("SettingsViewModel", "ViewModel Initializing - Loading settings...")
        loadSettings()
    }

    // loadSettings remains unchanged (uses viewModelScope internally)
    private fun loadSettings() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Coroutine launched for loadSettings")
            try {
                // Load Theme Settings
                Log.d("SettingsViewModel", "Loading theme settings...")
                val storedBackgroundChoiceName = dataStore.data.map { prefs -> prefs[BACKGROUND_CHOICE_KEY] ?: BackgroundChoice.WHITE.name }.first()
                val storedDarkMode = dataStore.data.map { prefs -> prefs[DARK_MODE_KEY] ?: false }.first()
                val backgroundChoice = try { BackgroundChoice.valueOf(storedBackgroundChoiceName) } catch (e: IllegalArgumentException) { BackgroundChoice.WHITE }
                val loadedThemeSettings = ThemeSettings(backgroundChoice = backgroundChoice, darkModeEnabled = storedDarkMode)
                _themeSettings.value = loadedThemeSettings
                _appState.update { it.copy(backgroundChoice = loadedThemeSettings.backgroundChoice, darkModeEnabled = loadedThemeSettings.darkModeEnabled) }
                AppThemeSettings.updateThemeSettings(loadedThemeSettings)
                Log.d("SettingsViewModel", "Theme settings loaded: $loadedThemeSettings")


                // Load Language Setting
                Log.d("SettingsViewModel", "Loading language setting...")
                val storedLanguage = dataStore.data.map { prefs -> prefs[LANGUAGE_KEY] ?: "en" }.first()
                Log.d("SettingsViewModel", "Loaded Language from DataStore: '$storedLanguage'")
                _currentLanguagePreference.value = storedLanguage
                _appState.update { it.copy(language = storedLanguage) }
                Log.d("SettingsViewModel", "Internal language state updated to: '$storedLanguage'")

                Log.d("SettingsViewModel", "Initial settings load complete (initial locale applied by MainActivity).")

            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to load settings from DataStore", e)
                _currentLanguagePreference.value = "en"
                _themeSettings.value = ThemeSettings()
                AppThemeSettings.updateThemeSettings(ThemeSettings())
                _appState.value = AppState()
            }
        }
    }


    // updateThemeSettings remains unchanged (uses viewModelScope internally)
    open fun updateThemeSettings(newThemeSettings: ThemeSettings) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Updating theme settings to: $newThemeSettings")
            try {
                dataStore.edit { prefs ->
                    prefs[BACKGROUND_CHOICE_KEY] = newThemeSettings.backgroundChoice.name
                    prefs[DARK_MODE_KEY] = newThemeSettings.darkModeEnabled
                }
                _themeSettings.value = newThemeSettings
                _appState.update { it.copy(backgroundChoice = newThemeSettings.backgroundChoice, darkModeEnabled = newThemeSettings.darkModeEnabled) }
                AppThemeSettings.updateThemeSettings(newThemeSettings)
                Log.d("SettingsViewModel", "Theme settings updated and saved successfully.")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to save theme settings", e)
            }
        }
    }


    // --- MODIFIED: Make this a suspend function ---
    open suspend fun updateLanguage(languageCode: String) {
        // Use lowercase comparison for safety
        if (languageCode.lowercase() == _currentLanguagePreference.value.lowercase()) {
            Log.d("SettingsViewModel", "Language '$languageCode' already set, skipping update.")
            return
        }
        Log.d("SettingsViewModel", "updateLanguage called with: '$languageCode'")

        // --- REMOVED: viewModelScope.launch { ... } ---
        // The caller (SettingsScreen) will provide the scope.

        Log.d("SettingsViewModel", "Attempting to save language '$languageCode' to DataStore...")
        try {
            // Save preference (use the code as passed, e.g., "en" or "et")
            // dataStore.edit is already a suspend function
            dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }
            Log.d("SettingsViewModel", "Successfully saved '$languageCode' to DataStore.")

            // Update internal state (safe to do after suspend completes)
            _currentLanguagePreference.value = languageCode
            _appState.update { it.copy(language = languageCode)}
            Log.d("SettingsViewModel", "Internal language state updated to '$languageCode'")

            // Apply locale change using AppCompatDelegate - THIS CALL REMAINS
            // It's generally safe and quick enough to keep here.
            applyLocale(languageCode)
            Log.d("SettingsViewModel", "Locale update applied via applyLocale for '$languageCode'. App restart will handle full UI update.")

        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to save/apply language '$languageCode'", e)
        }
        // --- END REMOVED viewModelScope.launch ---
    }

    // applyLocale remains unchanged (it's called by the now-suspend updateLanguage)
    private fun applyLocale(languageCode: String) {
        try {
            Log.d("SettingsViewModel", ">>> applyLocale attempting to set language code: '$languageCode'")
            // --- Using specific tags can sometimes help, but standard codes often work ---
            // val tagToApply = when (languageCode.lowercase()) {
            //    "et" -> "et-EE" // Use Estonian (Estonia) tag
            //    "en" -> "en-US" // Use English (US) tag
            //    else -> "en-US" // Default fallback to English (US)
            //}
            // --- Sticking to simple tags for now ---
            val tagToApply = languageCode.lowercase()

            Log.d("SettingsViewModel", "Applying locale tag: '$tagToApply'")
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(tagToApply)
            Log.d("SettingsViewModel", "Setting LocaleListCompat: ${appLocale.toLanguageTags()}")
            val appContext: Context = getApplication<TaskRabbitApplication>().applicationContext
            val localesBefore = appContext.resources.configuration.locales
            Log.d("SettingsViewModel", "VERIFY (BEFORE set): App Context locale list: ${localesBefore.toLanguageTags()}")

            AppCompatDelegate.setApplicationLocales(appLocale)
            Log.d("SettingsViewModel", "Successfully called AppCompatDelegate.setApplicationLocales.")

            val localesAfter = appContext.resources.configuration.locales
            Log.d("SettingsViewModel", "VERIFY (AFTER set): App Context locale list: ${localesAfter.toLanguageTags()}")

        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error applying locale for language code '$languageCode'", e)
        }
    }
}