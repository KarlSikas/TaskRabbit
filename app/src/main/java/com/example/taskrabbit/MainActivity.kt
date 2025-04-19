package com.example.taskrabbit

import TaskRabbitTheme
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taskrabbit.ui.screens.CalendarScreen
import com.example.taskrabbit.ui.screens.SettingsScreen
import com.example.taskrabbit.ui.screens.TaskListScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalContext

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
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(LocalContext.current.applicationContext as Application))
            val themeSettings by settingsViewModel.themeSettings.collectAsState()

            TaskRabbitTheme(darkTheme = themeSettings.darkModeEnabled) {
                // Wrap the content in MaterialTheme and Surface
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background // Apply background color from theme
                    ) {
                        val navController = rememberNavController()

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.TaskList.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.TaskList.route) {
                                    TaskListScreen(
                                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                                        onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                                        themeSettings = themeSettings
                                    )
                                }

                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        themeSettings = themeSettings,
                                        settingsViewModel = settingsViewModel
                                    )
                                }

                                composable(Screen.Calendar.route) {
                                    CalendarScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onDateSelected = { selectedDate ->
                                            println("Selected Date: $selectedDate")
                                            navController.popBackStack()
                                        },
                                        themeSettings = themeSettings
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}