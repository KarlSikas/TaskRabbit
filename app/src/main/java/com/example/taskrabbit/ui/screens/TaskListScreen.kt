package com.example.taskrabbit.ui.screens

import android.Manifest
import android.app.Application
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.taskrabbit.ui.theme.AppThemeSettings
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

    // Speech Recognition Launcher
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                if (results.isNotEmpty()) {
                    newTaskText = results[0]
                }
            }
        }
        isListeningForSpeech = false
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                speechRecognitionLauncher.launch(intent)
                isListeningForSpeech = true
            } catch (e: Exception) {
                isListeningForSpeech = false
            }
        } else {
            isListeningForSpeech = false
        }
    }

    Box( // Root Box for background
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
                    // Make TopAppBar transparent too if desired
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface, // Adjust if needed for contrast
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            // --- REMOVE bottomBar parameter ---
            // bottomBar = { ... }
            containerColor = Color.Transparent // Make Scaffold background transparent
        ) { paddingValues ->

            // --- Use a Box to layer the list and the floating input bar ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold (for status bar etc.)
            ) {
                // --- Task List Column ---
                // Needs bottom padding so the last item isn't hidden by the input bar
                val inputBarHeightEstimate = 80.dp // Adjust as needed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    // Add enough padding at the bottom to clear the floating input bar
                    contentPadding = PaddingValues(bottom = inputBarHeightEstimate + 16.dp)
                ) {
                    if (tasksForDate.isEmpty()) {
                        item { // Use item for single elements in LazyColumn
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize() // Fill the parent LazyColumn area
                                    .padding(bottom = inputBarHeightEstimate + 16.dp), // Center respecting bottom padding
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_tasks_for_date),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    } else {
                        items(tasksForDate, key = { task -> task.id }) { task ->
                            TaskItemCard(
                                task = task,
                                onDelete = { taskId -> viewModel.deleteTask(taskId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } // End LazyColumn

                // --- Floating Input Bar ---
                FloatingTaskInputBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Align to bottom of the Box
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp), // Padding to lift it off the edge and for horizontal margins
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
                ) // End FloatingTaskInputBar call

            } // End Box
        } // End Scaffold content lambda
    } // End Root Box
}

// --- NEW: Extracted Floating Input Bar Composable ---
@Composable
private fun FloatingTaskInputBar(
    modifier: Modifier = Modifier, // Pass modifier for alignment and padding
    newTaskText: String,
    isListeningForSpeech: Boolean,
    onTextChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onAddTask: () -> Unit
) {
    Surface( // Use Surface for elevation, shape, and background
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50), // Pill shape
        // --- Make it more transparent ---
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 6.dp // Add some elevation
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp), // Internal padding within the Surface
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.add_task_hint)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50), // Keep text field rounded too
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    // --- Make TextField background transparent to see Surface color ---
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent, // Hide border or use a subtle color
                    unfocusedBorderColor = Color.Transparent,
                    // Adjust text/placeholder color for contrast on the semi-transparent background
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            )
            // Mic Button
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.accessibility_voice_input),
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            // Add Task Button
            IconButton(onClick = onAddTask, enabled = newTaskText.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task),
                    tint = if (newTaskText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}
// --- End Floating Input Bar ---


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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            IconButton(onClick = { onDelete(task.id) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_task),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}