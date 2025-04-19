package com.example.taskrabbit.ui.screens

import android.Manifest
import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.res.painterResource
import com.example.taskrabbit.viewmodel.SettingsViewModel
import com.example.taskrabbit.ui.theme.BackgroundChoice
import androidx.compose.ui.draw.paint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    var newTaskText by remember { mutableStateOf("") }
    var isListeningForSpeech by remember { mutableStateOf(false) }
    var currentViewDate by remember { mutableStateOf(LocalDate.now()) }
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()

    val settingsViewModel: SettingsViewModel = viewModel()
    val themeSettings by settingsViewModel.themeSettings.collectAsState()

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
                false
            }
        } else {
            false
        }
    }

    fun getBackgroundResource(backgroundChoice: BackgroundChoice): Int? {
        return when (backgroundChoice) {
            BackgroundChoice.BUTTERFLY -> R.drawable.bg_butterfly
            BackgroundChoice.COLORFUL -> R.drawable.bg_colorful
            BackgroundChoice.CUTE -> R.drawable.bg_cute
            BackgroundChoice.FLOWERS -> R.drawable.bg_flowers
            BackgroundChoice.RAINBOW -> R.drawable.bg_rainbow
            BackgroundChoice.SHOOTING_STAR -> R.drawable.bg_shooting_star
            BackgroundChoice.SKELETON_HEAD -> R.drawable.bg_skeleton_head
            BackgroundChoice.WHITE -> null
        }
    }

    val backgroundResource = getBackgroundResource(themeSettings.backgroundChoice)

    LaunchedEffect(currentViewDate) {
        viewModel.loadTasksForDate(currentViewDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentViewDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(
                    if (backgroundResource != null) {
                        Modifier.paint(painterResource(id = backgroundResource))
                    } else {
                        Modifier.background(if (themeSettings.darkModeEnabled) Color.DarkGray else Color.White)
                    }
                )
        ) {
            TaskListContent(
                paddingValues = paddingValues,
                tasksForDate = tasksForDate,
                onDeleteTask = { taskId -> viewModel.deleteTask(taskId) }
            )
        }
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
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAddTask, enabled = newTaskText.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
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
    onDeleteTask: (Long) -> Unit
) {
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
                        onDelete = onDeleteTask
                    )
                    Divider()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title, // Corrected: use task.title instead of task.name
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = { onDelete(task.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}