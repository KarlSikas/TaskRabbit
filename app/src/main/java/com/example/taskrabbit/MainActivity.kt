package com.example.taskrabbit

// --- Keep existing imports ---
import android.os.Bundle
// --- Use AppCompatActivity ---
import androidx.appcompat.app.AppCompatActivity // <<< Correct Base Class
// --- END CHANGE ---
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.util.Log // <<< Keep this for other logs
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskrabbit.ui.screens.TaskListScreen
import com.example.taskrabbit.ui.screens.SettingsScreen
import com.example.taskrabbit.ui.screens.CalendarScreen
import com.example.taskrabbit.viewmodel.SettingsViewModel
import com.example.taskrabbit.viewmodel.TaskViewModel
import org.threeten.bp.LocalDate
import android.app.AlarmManager
// Removed duplicate Context import
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text // <<< ADDED IMPORT
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable // Added missing Composable import for AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource // <<< ADDED IMPORT
import com.example.taskrabbit.R // Ensure R is imported if not already


// --- Use AppCompatActivity ---
class MainActivity : AppCompatActivity() { // <<< Correct Base Class
// --- END CHANGE ---

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory(application as TaskRabbitApplication)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(application as TaskRabbitApplication)
    }

    // State for Exact Alarm Dialog
    private val showExactAlarmDialog = mutableStateOf(false)

    // State and Launcher for Notification Permission
    private val showNotificationPermissionRationale = mutableStateOf(false)
    // Use this directly for AppCompatActivity
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PermissionCheck", "POST_NOTIFICATIONS permission GRANTED by user.")
            } else {
                Log.w("PermissionCheck", "POST_NOTIFICATIONS permission DENIED by user.")
                Toast.makeText(this, "Notifications will not be shown without permission.", Toast.LENGTH_SHORT).show()
            }
            showNotificationPermissionRationale.value = false
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Call applyInitialLocale BEFORE super.onCreate()
        applyInitialLocale() // <<< RESTORED LOGIC VERSION IS BELOW
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            // --- TEST TEXT ---
            val settingsText = stringResource(id = R.string.settings)
            Log.d("MainActivity_TEST", "setContent is recomposing. Settings string: '$settingsText'")
            Text("TEST: $settingsText") // Display the text directly
            // --- END TEST TEXT ---

            // Remember state values
            var showAlarmDialogState by remember { showExactAlarmDialog }
            var showNotificationRationaleState by remember { showNotificationPermissionRationale }

            // Check/Request Notification Permission when composition starts
            LaunchedEffect(Unit) {
                checkAndRequestNotificationPermission()
            }

            // Create the NavController
            val navController = rememberNavController()


            // --- Main UI Structure ---
            // Exact Alarm Dialog
            if (showAlarmDialogState) {
                PermissionAlertDialog(
                    title = "Exact Alarm Permission Required",
                    text = "Task reminders need special permission to work reliably. Please grant the 'Alarms & reminders' permission in the app settings.",
                    confirmButtonText = "Go to Settings",
                    onConfirm = {
                        showAlarmDialogState = false
                        openExactAlarmSettings()
                    },
                    onDismiss = {
                        showAlarmDialogState = false
                        Toast.makeText(this@MainActivity, "Reminders may not work reliably without permission.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Notification Permission Rationale Dialog
            if (showNotificationRationaleState) {
                PermissionAlertDialog(
                    title = "Notification Permission Needed",
                    text = "To show task reminders as notifications, this app needs permission to post notifications. Please grant this permission when prompted.",
                    confirmButtonText = "Continue",
                    onConfirm = {
                        showNotificationRationaleState = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onDismiss = {
                        showNotificationRationaleState = false
                        Toast.makeText(this@MainActivity, "Notifications cannot be shown without permission.", Toast.LENGTH_SHORT).show()
                    }
                )
            }


            // --- NavHost ---
            NavHost(navController = navController, startDestination = "taskList") {
                composable("taskList") {
                    TaskListScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToCalendar = {
                            Log.d("Nav", "Navigating to Calendar") // Keep useful logs
                            navController.navigate("calendar")
                        },
                        viewModel = taskViewModel
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        settingsViewModel = settingsViewModel
                    )
                }
                composable("calendar") {
                    CalendarScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onDateSelected = { selectedDate: LocalDate ->
                            Log.d("Nav", "Date selected in Calendar: $selectedDate. Navigating back to TaskList.") // Keep useful logs
                            taskViewModel.selectDate(selectedDate)
                            navController.popBackStack()
                        },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
            }
            // --- End NavHost ---
        }
    }

    override fun onResume() {
        super.onResume()
        checkExactAlarmPermissionStatus()
    }

    // --- Locale function (Restored Original Logic with Logging) ---
    private fun applyInitialLocale() {
        // --- HARDCODED TEST REMOVED ---

        // --- RESTORED ORIGINAL LOGIC ---
        var currentLanguage: String
        try {
            // Get preference from Application class
            currentLanguage = (application as TaskRabbitApplication).getInitialLanguagePreference()
            Log.i("MainActivity", "Read language preference: '$currentLanguage'") // Log the read value
        } catch (e: Exception) {
            // Log error if reading preference fails
            Log.e("MainActivity", "CRASHED getting language preference!", e)
            currentLanguage = "en" // Default to English on error
        }

        // Determine the language tag to apply based on the preference
        val tagToApply = when (currentLanguage.lowercase()) {
            "et" -> "et"
            "en" -> "en"
            else -> "en" // Default to English if preference is unexpected
        }
        // --- END RESTORED LOGIC ---


        // Get the currently set application locales
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        // Log before attempting to set
        Log.d("MainActivity", "Applying locale: '$tagToApply'. Current AppCompat locales before setting: ${currentAppLocales.toLanguageTags()}")

        // Check if the desired locale is already set
        if (currentAppLocales.isEmpty || !currentAppLocales.toLanguageTags().contains(tagToApply)) {
            try {
                // Create the locale list for the desired language
                val appLocale = LocaleListCompat.forLanguageTags(tagToApply)
                // Apply the locale using AppCompatDelegate
                AppCompatDelegate.setApplicationLocales(appLocale)
                // Log after successful application
                Log.i("MainActivity", "AppCompatDelegate.setApplicationLocales called with: $tagToApply. New locales: ${AppCompatDelegate.getApplicationLocales().toLanguageTags()}")
            } catch (e: Exception) {
                // Log any error during application
                Log.e("MainActivity", "Error applying locale '$tagToApply'", e)
            }
        } else {
            // Log if the locale doesn't need changing
            Log.i("MainActivity", "Locale '$tagToApply' already set or included. No change needed. Current: ${currentAppLocales.toLanguageTags()}")
        }
    }
    // --- END Restored Locale function ---


    // --- Check Exact Alarm Permission (Keep as is) ---
    private fun checkExactAlarmPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w("PermissionCheck", "Exact alarm permission check: FAILED. Setting state to show Compose dialog.") // Keep useful logs
                showExactAlarmDialog.value = true
            } else {
                Log.d("PermissionCheck", if (alarmManager == null) "AlarmManager is null" else "Exact alarm permission check: PASSED.") // Keep useful logs
                showExactAlarmDialog.value = false
            }
        } else { Log.d("PermissionCheck", "Not on Android 12+. No SCHEDULE_EXACT_ALARM check needed.") } // Keep useful logs
    }

    // --- Open Exact Alarm Settings (Keep as is) ---
    private fun openExactAlarmSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
            Log.d("PermissionCheck", "Launched settings using ACTION_REQUEST_SCHEDULE_EXACT_ALARM.") // Keep useful logs
        } catch (e: Exception) {
            Log.e("PermissionCheck", "Failed to launch ACTION_REQUEST_SCHEDULE_EXACT_ALARM. Trying fallback.", e) // Keep useful logs
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(fallbackIntent)
                Log.d("PermissionCheck", "Launched fallback settings.") // Keep useful logs
            } catch (e2: Exception) {
                Log.e("PermissionCheck", "Failed to launch any settings.", e2) // Keep useful logs
                Toast.makeText(this, "Could not open settings. Please grant 'Alarms & reminders' permission manually.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Check and Request Notification Permission (Keep as is) ---
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("PermissionCheck", "POST_NOTIFICATIONS permission already granted.") // Keep useful logs
                }
                // Use `this` directly for AppCompatActivity
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.w("PermissionCheck", "POST_NOTIFICATIONS rationale needed. Setting state to show rationale dialog.") // Keep useful logs
                    showNotificationPermissionRationale.value = true
                }
                else -> {
                    Log.i("PermissionCheck", "Requesting POST_NOTIFICATIONS permission.") // Keep useful logs
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else { Log.d("PermissionCheck", "Not on Android 13+. No POST_NOTIFICATIONS check needed.") } // Keep useful logs
    }

    // --- Composable function for Dialogs (Using hardcoded strings & stringResource for Cancel) ---
    @Composable
    private fun PermissionAlertDialog(
        title: String,
        text: String,
        confirmButtonText: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel)) // Use string resource for Cancel
                }
            }
        )
    }
}