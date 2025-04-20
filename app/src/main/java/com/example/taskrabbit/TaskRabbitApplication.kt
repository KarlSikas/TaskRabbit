package com.example.taskrabbit

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.flow.Flow // Import Flow
import kotlinx.coroutines.flow.catch // Import catch
import kotlinx.coroutines.flow.first // Import first
import kotlinx.coroutines.flow.map // Import map
import kotlinx.coroutines.runBlocking // Import runBlocking
import java.io.IOException // Import IOException

// Preferences Key (moved here for Application access)
val LANGUAGE_KEY = stringPreferencesKey("language_preference")

// Top-level DataStore delegate
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TaskRabbitApplication : Application() {

    // Publicly accessible DataStore instance
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        private lateinit var instance: TaskRabbitApplication

        fun getAppContext(): Context {
            return instance.applicationContext
        }

        // Simplified DataStore access if needed, but prefer instance access
        fun getDataStore(): DataStore<Preferences> {
            return instance.dataStore
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this)
    }

    // --- NEW FUNCTION ---
    // Function to synchronously get the language preference on startup.
    // Use with caution - blocking operation on the main thread during startup.
    // Only suitable for very quick DataStore reads like this.
    fun getInitialLanguagePreference(): String {
        return runBlocking { // Use runBlocking for synchronous execution here
            dataStore.data
                .catch { exception ->
                    // Handle error, e.g., IOException from reading DataStore
                    if (exception is IOException) {
                        emit(emptyPreferences()) // Emit empty preferences on error
                    } else {
                        throw exception // Rethrow other exceptions
                    }
                }
                .map { preferences ->
                    preferences[LANGUAGE_KEY] ?: "en" // Default to "en"
                }
                .first() // Get the first emitted value
        }
    }
    // --- END NEW FUNCTION ---
}