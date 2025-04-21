package com.example.taskrabbit

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap // Import for Bitmap
import android.graphics.BitmapFactory // Import for BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskrabbit.R // Make sure R is imported correctly

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "com.example.taskrabbit.EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "com.example.taskrabbit.EXTRA_TASK_TITLE"
        // Channel ID is only used on API 26+
        const val CHANNEL_ID = "task_reminders"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ReminderReceiver", "onReceive triggered")
        // Use requireContext() for slightly cleaner null safety if you expect context to never be null here
        // Or keep the explicit check which is also fine.
        if (context == null || intent == null) {
            Log.e("ReminderReceiver", "Context or Intent is null")
            return
        }

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: context.getString(R.string.default_task_title)

        if (taskId == -1L) {
            Log.e("ReminderReceiver", "Invalid Task ID received")
            return
        }

        Log.d("ReminderReceiver", "Received reminder for Task ID: $taskId, Title: $taskTitle")

        // Create notification channel (needed for API 26+). Safe to call multiple times.
        createNotificationChannel(context)

        // Intent to launch app when notification is clicked
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // putExtra(EXTRA_TASK_ID, taskId) // Optional: if MainActivity needs to know
        }

        // PendingIntent flags: FLAG_IMMUTABLE is required for API 31+, recommended otherwise.
        // FLAG_UPDATE_CURRENT ensures extras are updated if the PendingIntent is reused.
        val pendingActivityIntent: PendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(), // Use task ID for request code uniqueness
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Load Large Icon Bitmap (works on all API levels)
        val largeIconBitmap: Bitmap? = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.my_notification_image)
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Error loading large icon bitmap (my_notification_image.png)", e)
            null
        }

        // Build the notification using NotificationCompat for backward compatibility
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure ic_notification.xml exists
            .setContentTitle(context.getString(R.string.task_reminder))
            .setContentText(taskTitle)
            .setLargeIcon(largeIconBitmap) // Optional large icon
            // Priority is used for API < 26. Importance is set on the channel for API 26+.
            // NotificationCompat handles mapping this correctly.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Helps system classify notification
            .setContentIntent(pendingActivityIntent) // Intent to launch on click
            .setAutoCancel(true) // Dismiss notification when clicked
            // Set default notification sound, vibration, etc.
            // On API 26+, these are configured on the channel, but setting here provides fallback.
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Use NotificationManagerCompat for compatibility
        val notificationManager = NotificationManagerCompat.from(context)

        // Check for POST_NOTIFICATIONS permission ONLY on Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("ReminderReceiver", "POST_NOTIFICATIONS permission not granted (API 33+). Cannot show notification for Task ID: $taskId.")
                // NOTE: You cannot request permission from a BroadcastReceiver.
                // The permission needs to be requested from an Activity context earlier.
                return // Exit without showing notification if permission denied on API 33+
            }
        }
        // On API levels 26-32, no special permission is needed beyond declaring the receiver.

        // Show the notification. Use task ID as notification ID.
        try {
            // NotificationManagerCompat handles showing the notification correctly across versions.
            notificationManager.notify(taskId.toInt(), builder.build())
            Log.d("ReminderReceiver", "Notification shown for Task ID: $taskId")
        } catch (e: SecurityException) {
            // This might happen on specific devices or if permissions change unexpectedly.
            Log.e("ReminderReceiver", "SecurityException showing notification for Task ID: $taskId. Check logs.", e)
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Generic Exception showing notification for Task ID: $taskId.", e)
        }
    }

    // Creates the Notification Channel, required ONLY on API 26+
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 'O' is API 26
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            // Importance maps to priority for older versions via NotificationCompat
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Configure channel specifics (optional)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                // enableLights(true)
                // lightColor = Color.RED
            }
            // Register the channel with the system; you can call this repeatedly
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                notificationManager.createNotificationChannel(channel)
                Log.d("ReminderReceiver", "Notification channel '$CHANNEL_ID' ensured.")
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error creating notification channel '$CHANNEL_ID'", e)
            }
        }
    }
}