package com.example.taskrabbit.ui.screens

import android.Manifest
import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Keep other icons like DateRange, Settings, Mic, Add, Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.R
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.viewmodel.TaskViewModel
import com.example.taskrabbit.ui.theme.ThemeSettings
import com.example.taskrabbit.ui.theme.AppThemeSettings
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToSettings: () -> Unit, // Callback for navigating to the Settings screen
    onNavigateToCalendar: () -> Unit, // Callback for navigating to the Calendar screen
    // Removed onNavigateToBackgroundSelector callback as the button is removed
    viewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    var newTaskText by remember { mutableStateOf("") }
    var isListeningForSpeech by remember { mutableStateOf(false) }
    var currentViewDate by remember { mutableStateOf(LocalDate.now()) }
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()
    val appThemeSettings = AppThemeSettings()
    val themeSettings by appThemeSettings.collectAsState(initial = ThemeSettings())

    // Speech recognition launcher
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
            if (results.isNotEmpty()) {
                newTaskText = results[0]
            }
        }
        isListeningForSpeech = false
    }

    // Permission launcher for accessing microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isListeningForSpeech = if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                speechRecognitionLauncher.launch(intent)
                true
            } catch (e: Exception) {
                // It's good practice to handle potential exceptions here
                // e.g., log the error or show a message to the user
                false
            }
        } else {
            // Handle the case where permission is denied, maybe show a message
            false
        }
    }

    // Load tasks for the selected date whenever the date changes
    LaunchedEffect(currentViewDate) {
        viewModel.loadTasksForDate(currentViewDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.my_tasks)) },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    // IconButton for Image/Background Selector has been removed.
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            TaskInputBar(
                newTaskText = newTaskText,
                isListeningForSpeech = isListeningForSpeech,
                onTextChange = { newTaskText = it },
                onMicClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onAddTask = {
                    if (newTaskText.isNotBlank()) {
                        viewModel.addTask(newTaskText, currentViewDate)
                        newTaskText = "" // Clear text field after adding
                    }
                }
            )
        }
    ) { paddingValues ->
        TaskListContent(
            paddingValues = paddingValues,
            tasksForDate = tasksForDate,
            currentViewDate = currentViewDate,
            onDeleteTask = { taskId -> viewModel.deleteTask(taskId) }
        )
    }
}

@Composable
private fun TaskInputBar(
    newTaskText: String,
    isListeningForSpeech: Boolean,
    onTextChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onAddTask: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.add_task_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true // Ensures the text field doesn't expand vertically
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAddTask, enabled = newTaskText.isNotBlank()) { // Disable add button if text is blank
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    // Adjust tint based on enabled state if desired
                    tint = if (newTaskText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun TaskListContent(
    paddingValues: PaddingValues,
    tasksForDate: List<TaskItem>,
    currentViewDate: LocalDate,
    onDeleteTask: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply padding from Scaffold
    ) {
        // Display the current date being viewed
        Text(
            text = currentViewDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), // Consistent date format
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp), // Padding around the date text
            color = MaterialTheme.colorScheme.primary // Use primary color for emphasis
        )

        // Conditional content based on whether tasks exist for the date
        if (tasksForDate.isEmpty()) {
            // Show a message if there are no tasks
            Box(
                modifier = Modifier.fillMaxSize(), // Take up remaining space
                contentAlignment = Alignment.Center // Center the message
            ) {
                Text(stringResource(R.string.no_tasks_for_date))
            }
        } else {
            // Display the list of tasks if any exist
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize() // Take up remaining space
                    .padding(horizontal = 16.dp) // Horizontal padding for list items
            ) {
                items(tasksForDate, key = { task -> task.id }) { task -> // Use task id as key for better performance
                    TaskItemCard(
                        task = task,
                        onDelete = onDeleteTask // Pass the delete action
                    )
                    Divider() // Add a divider between task items
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskItem,
    onDelete: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Padding for each task item row
        verticalAlignment = Alignment.CenterVertically, // Align items vertically in the center
        horizontalArrangement = Arrangement.SpaceBetween // Space out text and button
    ) {
        Text(
            text = task.title,
            modifier = Modifier.weight(1f).padding(end = 8.dp) // Allow text to take space and add padding before button
        )
        IconButton(onClick = { onDelete(task.id) }) { // Trigger delete action on click
            Icon(Icons.Default.Delete, contentDescription = "Delete Task") // Use specific content description
        }
    }
}