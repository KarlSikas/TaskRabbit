package com.example.taskrabbit

import TaskRabbitTheme // Assuming this import is correct
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
// Removed unused dp import
// import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taskrabbit.ui.screens.CalendarScreen
import com.example.taskrabbit.ui.screens.SettingsScreen
import com.example.taskrabbit.ui.screens.TaskListScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.taskrabbit.ui.theme.AppThemeSettings // Import AppThemeSettings

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // --- Collect theme settings directly from AppThemeSettings ---
            // This ensures the TaskRabbitTheme composable reacts to changes
            val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
            // --- End collection ---

            // ViewModel needed for SettingsScreen
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(LocalContext.current.applicationContext as Application))

            // Apply dark theme based on the collected state
            TaskRabbitTheme(darkTheme = currentThemeSettings.darkModeEnabled) {
                // Surface is handled by Scaffold now, so we might not need an extra one here
                // MaterialTheme { // TaskRabbitTheme likely already applies MaterialTheme
                // Surface(
                //     modifier = Modifier.fillMaxSize(),
                //     color = MaterialTheme.colorScheme.background // Scaffold handles this
                // ) {
                val navController = rememberNavController()

                // Scaffold provides the basic layout structure (app bars, etc.)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.TaskList.route,
                        // Apply padding from Scaffold to avoid content drawing behind system bars
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.TaskList.route) {
                            TaskListScreen(
                                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) }
                                // Removed themeSettings = themeSettings
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                // Still pass themeSettings here as the composable expects it,
                                // even though it primarily uses the collected state internally.
                                // Alternatively, you could remove the parameter from SettingsScreen too,
                                // but passing it might be useful for previews or initial state.
                                themeSettings = currentThemeSettings,
                                settingsViewModel = settingsViewModel
                            )
                        }

                        composable(Screen.Calendar.route) {
                            CalendarScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onDateSelected = { selectedDate ->
                                    println("Selected Date: $selectedDate") // Keep or replace with actual logic
                                    // Maybe navigate back to TaskList with the selected date?
                                    // viewModel.selectDate(selectedDate) // Example
                                    navController.popBackStack()
                                }
                                // Removed themeSettings = themeSettings
                            )
                        }
                    }
                }
                // } // End Surface
                // } // End MaterialTheme
            } // End TaskRabbitTheme
        }
    }
}
