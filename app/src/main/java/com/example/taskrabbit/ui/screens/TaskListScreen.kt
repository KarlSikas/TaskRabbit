package com.example.taskrabbit.ui.screens

import android.Manifest
import android.app.Application
import android.app.Activity // Import Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image // Import Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Import collectAsState extension
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskrabbit.R
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.viewmodel.TaskViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import com.example.taskrabbit.ui.theme.AppThemeSettings // Import AppThemeSettings
import androidx.compose.ui.graphics.painter.Painter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)

    var newTaskText by remember { mutableStateOf("") }
    var isListeningForSpeech by remember { mutableStateOf(false) }
    var currentViewDate by remember { mutableStateOf(LocalDate.now()) }
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()

    // --- FIX 1: Restore Speech Recognition Launcher ---
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult() // Specify the contract
    ) { result ->
        // Specify the result handling logic
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                if (results.isNotEmpty()) {
                    newTaskText = results[0]
                }
            }
        }
        isListeningForSpeech = false
    }

    // --- FIX 2: Restore Permission Launcher ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission() // Specify the contract
    ) { isGranted ->
        // Specify the result handling logic
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...") // Optional prompt
                }
                speechRecognitionLauncher.launch(intent)
                isListeningForSpeech = true
            } catch (e: Exception) {
                // Handle exception (e.g., no speech recognition support)
                isListeningForSpeech = false
            }
        } else {
            // Handle permission denial (e.g., show a message)
            isListeningForSpeech = false
        }
    }
    // --- End FIX 1 & 2 ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
    ) {
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentViewDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.calendar))
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                        }
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
                            newTaskText = ""
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (tasksForDate.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_tasks_for_date))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(tasksForDate, key = { task -> task.id }) { task ->
                            TaskItemCard(
                                task = task,
                                onDelete = { taskId -> viewModel.deleteTask(taskId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        } // End Scaffold
    } // End Root Box
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
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
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
                singleLine = true
                // --- FIX 3: Remove problematic colors parameter ---
                // colors = TextFieldDefaults.outlinedTextFieldColors(
                //     // Customize based on theme/contrast needs
                // )
                // --- End FIX 3 ---
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.accessibility_voice_input),
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAddTask, enabled = newTaskText.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task),
                    tint = if (newTaskText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskItem,
    onDelete: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = { onDelete(task.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_task),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}