package com.example.taskrabbit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.TaskItem // <<< ADDED Import for TaskItem
import com.example.taskrabbit.util.ReminderScheduler // <<< Import your scheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// import kotlinx.coroutines.withContext // No longer needed for this specific call

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "onReceive triggered with action: $action")

        // Check for boot completed or quickboot actions
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device has booted. Attempting to reschedule reminders...")

            // Use goAsync to keep the receiver alive while we launch a coroutine
            val pendingResult: PendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO) // Use IO dispatcher for database access

            scope.launch {
                var rescheduleCount = 0 // Initialize count outside try block
                try {
                    // Get DAO instance (use application context to avoid leaks)
                    val appContext = context.applicationContext
                    val taskDao = AppDatabase.getDatabase(appContext).taskDao()

                    // --- Fetch tasks needing reminders using the CORRECT DAO function ---
                    // Calling the suspend function directly within the coroutine scope
                    val tasksToReschedule: List<TaskItem> = taskDao.getTasksWithPotentialReminders() // <<< CORRECTED DAO FUNCTION CALL
                    Log.d("BootReceiver", "Fetched ${tasksToReschedule.size} tasks with potential reminders.") // <<< UPDATED Log message
                    // --- End Fetching ---

                    // Instantiate scheduler
                    val scheduler = ReminderScheduler(appContext) // Pass application context

                    // Iterate over the correctly typed list
                    tasksToReschedule.forEach { task -> // <<< UPDATED variable name
                        // Check if task has valid reminder info (this check might be redundant now but safe)
                        if (task.reminderMinutesBefore != null && task.taskTime != null && task.dueDate != null /* && !task.isCompleted */) {
                            // Properties like task.id, task.title, etc., should now resolve
                            Log.d("BootReceiver", "Rescheduling reminder for task ID: ${task.id} ('${task.title}')")
                            scheduler.schedule(task) // Call the scheduling logic
                            rescheduleCount++
                        } else {
                            Log.w("BootReceiver", "Task ID ${task.id} fetched but missing necessary reminder info. Skipping reschedule.")
                        }
                    }
                    Log.d("BootReceiver", "Rescheduling attempt finished. $rescheduleCount reminders rescheduled.")

                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                } finally {
                    // Always call finish() when done to release the wake lock
                    pendingResult.finish()
                    Log.d("BootReceiver", "goAsync finished.")
                }
            }
        }
    }
}