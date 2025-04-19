package com.example.taskrabbit

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
import com.example.taskrabbit.ui.theme.TaskRabbitTheme
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel
import com.example.taskrabbit.viewmodel.SettingsViewModel // Import SettingsViewModel

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
            // Get the SettingsViewModel
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeSettings by settingsViewModel.themeSettings.collectAsState()

            TaskRabbitTheme {
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
                                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(Screen.Calendar.route) {
                            CalendarScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onDateSelected = { selectedDate ->
                                    println("Selected Date: $selectedDate")
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}