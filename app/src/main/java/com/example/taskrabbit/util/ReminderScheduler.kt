package com.example.taskrabbit.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.taskrabbit.ReminderReceiver // Import your receiver
import com.example.taskrabbit.data.TaskItem
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
// Removed unused ChronoUnit and TimeUnit imports for cleanliness
// import org.threeten.bp.temporal.ChronoUnit
// import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) { // Needs Context to get AlarmManager

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: TaskItem) {
        // --- Input Validation ---
        if (task.id <= 0L) { // Ensure task has a valid ID from the database
            Log.e("ReminderScheduler", "Cannot schedule reminder for task with invalid ID: ${task.id}")
            return
        }
        if (task.reminderMinutesBefore == null || task.taskTime == null || task.dueDate == null) {
            Log.w("ReminderScheduler", "Task ID ${task.id} missing reminder time, task time, or due date. Cannot schedule.")
            // Optionally cancel any existing alarm for this ID
            cancel(task.id)
            return
        }

        // --- Calculate Trigger Time ---
        val taskDateTime = LocalDateTime.of(task.dueDate, task.taskTime)
        val reminderDateTime = taskDateTime.minusMinutes(task.reminderMinutesBefore.toLong())
        val triggerAtMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()

        // Don't schedule reminders for the past
        if (triggerAtMillis <= nowMillis) {
            Log.w("ReminderScheduler", "Reminder time for task ID ${task.id} ($reminderDateTime) is in the past. Not scheduling.")
            return
        }

        // --- Create Intent & PendingIntent ---
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            // Pass necessary data to the receiver
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderReceiver.EXTRA_TASK_TITLE, task.title)
        }

        // Use task ID as request code for uniqueness
        // FLAG_IMMUTABLE is required for targetSdk 31+
        // FLAG_UPDATE_CURRENT ensures extras are updated if rescheduled
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(), // Unique request code based on task ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- Schedule using AlarmManager ---
        try {
            // ===>>> ADDED DETAILED LOGGING FOR PERMISSION CHECK <<<===
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // S is API 31 (Android 12)
                Log.d("ReminderScheduler", "Checking SCHEDULE_EXACT_ALARM permission on Android 12+ (API ${Build.VERSION.SDK_INT})")
                // Get the AlarmManager service again inside the check, just in case there are context issues (unlikely but safe)
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val canSchedule = am.canScheduleExactAlarms()
                Log.d("ReminderScheduler", "Result of alarmManager.canScheduleExactAlarms(): $canSchedule") // <<< Log the result
                if (!canSchedule) {
                    Log.e("ReminderScheduler", "Cannot schedule exact alarms check failed. Missing SCHEDULE_EXACT_ALARM permission or user denied setting.")
                    // TODO: Consider guiding user to settings or using inexact alarm as fallback
                    return // Exit if permission check fails
                } else {
                    Log.d("ReminderScheduler", "SCHEDULE_EXACT_ALARM permission check passed.")
                }
            } else {
                Log.d("ReminderScheduler", "Not on Android 12+ (API ${Build.VERSION.SDK_INT}). No SCHEDULE_EXACT_ALARM check needed.")
            }
            // ===>>> END ADDED LOGGING <<<===

            Log.d("ReminderScheduler", "Attempting to schedule alarm for task ID ${task.id} at $reminderDateTime ($triggerAtMillis)") // <<< Added log before setExact
            // Use the initially retrieved alarmManager instance
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, // Wake device up if needed
                triggerAtMillis,
                pendingIntent
            )
            // Log success *after* the call to setExactAndAllowWhileIdle
            Log.d("ReminderScheduler", "Successfully scheduled reminder for task ID ${task.id} at $reminderDateTime ($triggerAtMillis)")

        } catch (e: SecurityException) {
            // This might catch other security issues if the permission check somehow passed but scheduling failed.
            Log.e("ReminderScheduler", "SecurityException scheduling alarm for task ID ${task.id}. Check logs.", e)
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Generic Exception scheduling alarm for task ID ${task.id}", e)
        }
    }

    fun cancel(taskId: Long) {
        if (taskId <= 0L) {
            Log.e("ReminderScheduler", "Cannot cancel reminder for invalid task ID: $taskId")
            return
        }
        Log.d("ReminderScheduler", "Attempting to cancel reminder for task ID $taskId")

        // Recreate the *exact same* PendingIntent used for scheduling
        val intent = Intent(context, ReminderReceiver::class.java) // Target the same receiver
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(), // Must use the same request code (task ID)
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use same flags
        )

        // Cancel the alarm associated with the PendingIntent
        alarmManager.cancel(pendingIntent)
        // It's good practice to also cancel the PendingIntent itself if no longer needed, though AlarmManager.cancel usually suffices
        // pendingIntent.cancel()
        Log.d("ReminderScheduler", "Cancelled potential reminder for task ID $taskId")
    }
}