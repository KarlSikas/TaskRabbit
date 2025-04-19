package com.example.taskrabbit

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.threetenabp.AndroidThreeTen

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TaskRabbitApplication : Application() {

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        private lateinit var instance: TaskRabbitApplication

        fun getAppContext(): Context {
            return instance.applicationContext
        }

        fun getDataStore(): DataStore<Preferences> {
            return instance.dataStore
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this) // Initialize ThreeTenABP
    }
}