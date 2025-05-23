package com.example.taskrabbit

// --- Keep existing imports ---
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
// REMOVED: NavType and navArgument as TaskDetails route is removed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskrabbit.ui.screens.TaskListScreen // Use the TaskListScreen WITHOUT onNavigateToTaskDetails param
import com.example.taskrabbit.ui.screens.SettingsScreen
import com.example.taskrabbit.ui.screens.CalendarScreen
// REMOVED: TaskDetailsScreen import
import com.example.taskrabbit.viewmodel.SettingsViewModel
import com.example.taskrabbit.viewmodel.TaskViewModel
import org.threeten.bp.LocalDate
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.example.taskrabbit.R
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.taskrabbit.ui.theme.TaskRabbitTheme
import com.example.taskrabbit.ui.theme.BackgroundChoice
import com.example.taskrabbit.ui.theme.AppState


// Define your Screen sealed class (REMOVED TaskDetails)
sealed class Screen(val route: String) {
    object TaskList : Screen("taskList")
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    // REMOVED: object TaskDetails <<-- THIS LINE IS GONE
}


class MainActivity : AppCompatActivity() {

    // --- ViewModels and Permission States/Launchers (No Changes) ---
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory(application as TaskRabbitApplication)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(application as TaskRabbitApplication)
    }
    private val showExactAlarmDialog = mutableStateOf(false)
    private val showNotificationPermissionRationale = mutableStateOf(false)
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
        applyInitialLocale()
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            val appState by settingsViewModel.appState.collectAsState()
            var showAlarmDialogState by remember { showExactAlarmDialog }
            var showNotificationRationaleState by remember { showNotificationPermissionRationale }

            LaunchedEffect(Unit) {
                checkAndRequestNotificationPermission()
            }

            val navController = rememberNavController()

            TaskRabbitTheme(
                darkTheme = appState.darkModeEnabled,
                backgroundChoice = appState.backgroundChoice
            ) {

                // --- Permission Dialogs (No Changes) ---
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
                NavHost(navController = navController, startDestination = Screen.TaskList.route) {
                    composable(Screen.TaskList.route) {
                        TaskListScreen(
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToCalendar = {
                                Log.d("Nav", "Navigating to Calendar")
                                navController.navigate(Screen.Calendar.route)
                            },
                            // REMOVED: onNavigateToTaskDetails = { taskId -> ... }, <<-- THIS LINE IS GONE
                            viewModel = taskViewModel
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            settingsViewModel = settingsViewModel
                        )
                    }
                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onDateSelected = { selectedDate: LocalDate ->
                                Log.d("Nav", "Date selected in Calendar: $selectedDate. Navigating back to TaskList.")
                                taskViewModel.selectDate(selectedDate)
                                navController.popBackStack()
                            },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }

                    // REMOVED: composable block for Screen.TaskDetails.route <<-- THIS BLOCK IS GONE

                } // --- End NavHost ---
            } // --- End TaskRabbitTheme ---
        }
    }

    // --- onResume, applyInitialLocale, Permission Checks/Requests, PermissionAlertDialog (No Changes) ---
    override fun onResume() {
        super.onResume()
        checkExactAlarmPermissionStatus()
    }

    private fun applyInitialLocale() {
        var currentLanguage: String
        try {
            currentLanguage = (application as TaskRabbitApplication).getInitialLanguagePreference()
            Log.i("MainActivity", "Read language preference: '$currentLanguage'")
        } catch (e: Exception) {
            Log.e("MainActivity", "CRASHED getting language preference!", e)
            currentLanguage = "en"
        }
        val tagToApply = when (currentLanguage.lowercase()) {
            "et" -> "et"
            else -> "en"
        }
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        Log.d("MainActivity", "Applying locale: '$tagToApply'. Current AppCompat locales before setting: ${currentAppLocales.toLanguageTags()}")
        if (currentAppLocales.isEmpty || !currentAppLocales.toLanguageTags().contains(tagToApply)) {
            try {
                val appLocale = LocaleListCompat.forLanguageTags(tagToApply)
                AppCompatDelegate.setApplicationLocales(appLocale)
                Log.i("MainActivity", "AppCompatDelegate.setApplicationLocales called with: $tagToApply. New locales: ${AppCompatDelegate.getApplicationLocales().toLanguageTags()}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error applying locale '$tagToApply'", e)
            }
        } else {
            Log.i("MainActivity", "Locale '$tagToApply' already set or included. No change needed. Current: ${currentAppLocales.toLanguageTags()}")
        }
    }

    private fun checkExactAlarmPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w("PermissionCheck", "Exact alarm permission check: FAILED. Setting state to show Compose dialog.")
                showExactAlarmDialog.value = true
            } else {
                Log.d("PermissionCheck", if (alarmManager == null) "AlarmManager is null" else "Exact alarm permission check: PASSED.")
                showExactAlarmDialog.value = false
            }
        } else { Log.d("PermissionCheck", "Not on Android 12+. No SCHEDULE_EXACT_ALARM check needed.") }
    }

    private fun openExactAlarmSettings() {
        val intent = Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
            Log.d("PermissionCheck", "Launched settings using ACTION_REQUEST_SCHEDULE_EXACT_ALARM.")
        } catch (e: Exception) {
            Log.e("PermissionCheck", "Failed to launch ACTION_REQUEST_SCHEDULE_EXACT_ALARM. Trying fallback.", e)
            val fallbackIntent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(fallbackIntent)
                Log.d("PermissionCheck", "Launched fallback settings.")
            } catch (e2: Exception) {
                Log.e("PermissionCheck", "Failed to launch any settings.", e2)
                Toast.makeText(this, "Could not open settings. Please grant 'Alarms & reminders' permission manually.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("PermissionCheck", "POST_NOTIFICATIONS permission already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.w("PermissionCheck", "POST_NOTIFICATIONS rationale needed. Setting state to show rationale dialog.")
                    showNotificationPermissionRationale.value = true
                }
                else -> {
                    Log.i("PermissionCheck", "Requesting POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else { Log.d("PermissionCheck", "Not on Android 13+. No POST_NOTIFICATIONS check needed.") }
    }

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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}