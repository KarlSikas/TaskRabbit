package com.example.taskrabbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.taskrabbit.data.BackgroundImage
import com.example.taskrabbit.ui.screens.CalendarScreen
import com.example.taskrabbit.ui.screens.SettingsScreen
import com.example.taskrabbit.ui.screens.TaskListScreen
import com.example.taskrabbit.ui.theme.TaskRabbitTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            TaskRabbitTheme {
                val navController = rememberNavController()
                var selectedBackground by remember { mutableStateOf<BackgroundImage?>(null) }

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
                                },
                                selectedBackground = selectedBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    selectedBackground: BackgroundImage?
) {
    val currentDate = remember { LocalDate.now() }
    val dateList = remember { generateDates(currentDate) }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(text = "Calendar") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn {
            items(dateList) { date ->
                val isToday = date.isEqual(currentDate)
                DateRow(
                    date = date,
                    isToday = isToday,
                    onClick = { onDateSelected(date) },
                    selectedBackground = selectedBackground
                )
            }
        }
    }
}

@Composable
fun DateRow(
    date: LocalDate,
    isToday: Boolean,
    onClick: () -> Unit,
    selectedBackground: BackgroundImage?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .background(if (isToday) Color.LightGray else Color.Transparent)
            .padding(16.dp)
    ) {
        if (isToday && selectedBackground != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedBackground.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = selectedBackground.name,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

fun generateDates(startDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    for (i in -30..30) {
        dates.add(startDate.plusDays(i.toLong()))
    }
    return dates
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TaskRabbitTheme {
        Greeting("Android")
    }
}
