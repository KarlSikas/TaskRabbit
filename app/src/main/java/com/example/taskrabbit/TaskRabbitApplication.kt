package com.example.taskrabbit

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class TaskRabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}