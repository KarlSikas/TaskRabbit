package com.example.taskrabbit

import android.app.Application
import android.content.Context
import android.util.Log // Keep Log import for error logging
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
// Removed unused import: import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.security.EncryptionHandler
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import java.io.IOException
import java.lang.RuntimeException
import java.lang.IllegalStateException // Import explicitly
// Removed unused import: import java.security.GeneralSecurityException

// Define LANGUAGE_KEY if it's not already defined elsewhere accessible
val LANGUAGE_KEY = stringPreferencesKey("language_preference")

// Define the top-level DataStore delegate
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TaskRabbitApplication : Application() {

    // Use the application context's DataStore instance
    val dataStore: DataStore<Preferences> get() = this.applicationContext.dataStore

    val database: AppDatabase by lazy {
        // Log removed: "DATABASE_INIT: Attempting to initialize database instance..."
        val passphraseBytes = try {
            // Log removed: "DATABASE_INIT: Calling EncryptionHandler.getDatabasePassphraseBytes"
            EncryptionHandler.getDatabasePassphraseBytes(this)
        } catch (e: SecurityException) {
            Log.e("TaskRabbitApp", "DATABASE_INIT: FATAL - SecurityException getting passphrase!", e) // Keep error log
            throw RuntimeException("Cannot initialize database: Security setup failed.", e)
        } catch (e: Exception) {
            Log.e("TaskRabbitApp", "DATABASE_INIT: FATAL - Unexpected error getting passphrase!", e) // Keep error log
            throw RuntimeException("Cannot initialize database: Unexpected security error.", e)
        }
        // Log removed: "DATABASE_INIT: Successfully obtained passphrase bytes."

        val factory = try {
            // Log removed: "DATABASE_INIT: Creating SupportFactory"
            SupportFactory(passphraseBytes)
        } catch (e: Throwable) {
            Log.e("TaskRabbitApp", "DATABASE_INIT: FATAL - Error creating SupportFactory!", e) // Keep error log
            throw RuntimeException("Cannot initialize database: Failed to create SQLCipher factory.", e)
        }

        // Log removed: "DATABASE_INIT: Calling AppDatabase.getDatabase"
        AppDatabase.getDatabase(
            context = this,
            factory = factory
        )
        // Log removed: ".also { Log.i(... Database instance successfully created.) }"
        // The .also block is removed as it only contained the log statement.
    }

    companion object {
        private lateinit var instance: TaskRabbitApplication

        fun getAppContext(): Context {
            if (!::instance.isInitialized) {
                Log.e("TaskRabbitApp", "Companion: getAppContext called before instance was initialized!") // Keep error log
                throw IllegalStateException("TaskRabbitApplication instance not available.")
            }
            return instance.applicationContext
        }

        fun getDataStore(): DataStore<Preferences> {
            if (!::instance.isInitialized) {
                Log.e("TaskRabbitApp", "Companion: getDataStore called before instance was initialized!") // Keep error log
                throw IllegalStateException("TaskRabbitApplication instance not available.")
            }
            return instance.dataStore
        }
    }

    override fun onCreate() {
        // Log removed: "ON_CREATE: STARTING"
        super.onCreate()
        // Log removed: "ON_CREATE: super.onCreate() finished"
        instance = this
        // Log removed: "ON_CREATE: instance set"
        try {
            // Log removed: "ON_CREATE: Calling AndroidThreeTen.init()"
            AndroidThreeTen.init(this)
            // Log removed: "ON_CREATE: AndroidThreeTen.init() finished successfully"
        } catch (t: Throwable) {
            Log.e("TaskRabbitApp", "ON_CREATE: CRASH during AndroidThreeTen.init()!", t) // Keep error log
            throw RuntimeException("Crash during AndroidThreeTen init", t)
        }
        // Log removed: "ON_CREATE: Application onCreate completed."
    }

    fun getInitialLanguagePreference(): String {
        // Log removed: "LANG_PREF: Attempting to read initial language preference"
        return try {
            runBlocking {
                dataStore.data
                    .catch { exception ->
                        if (exception is IOException) {
                            Log.e("TaskRabbitApp", "LANG_PREF: IOException reading DataStore.", exception) // Keep error log
                            emit(emptyPreferences())
                        } else {
                            Log.e("TaskRabbitApp", "LANG_PREF: Non-IOException reading DataStore.", exception) // Keep error log
                            throw RuntimeException("Error reading language preference", exception)
                        }
                    }
                    .map { preferences ->
                        val lang = preferences[LANGUAGE_KEY] ?: "en"
                        // Log removed: "LANG_PREF: Mapped preference to '$lang'"
                        lang
                    }
                    .first()
            }
            // Log removed: ".also { lang -> Log.d(... Final language preference read: '$lang')}"
            // The .also block is removed as it only contained the log statement.
        } catch (e: Exception) {
            Log.e("TaskRabbitApp", "LANG_PREF: CRITICAL - Error in runBlocking/getInitialLanguagePreference!", e) // Keep error log
            "en" // Return default on critical error
        }
    }
}