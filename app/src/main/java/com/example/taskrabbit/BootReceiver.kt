package com.example.taskrabbit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device has booted. Rescheduling notifications...")
            // TODO: Reschedule your notifications here.
            // You'll likely need to access your TaskViewModel or a similar
            // data source to get the list of tasks with reminders.
        }
    }
}