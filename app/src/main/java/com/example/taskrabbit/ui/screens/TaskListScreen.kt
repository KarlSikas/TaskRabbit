package com.example.taskrabbit.ui.screens

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// Removed ExperimentalFoundationApi as combinedClickable is no longer used
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Keep clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.taskrabbit.R
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.ui.theme.AppThemeSettings
import com.example.taskrabbit.viewmodel.TaskViewModel
import kotlinx.coroutines.launch // Keep if needed for other async ops
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

// --- Constants (No Changes) ---
private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.POST_NOTIFICATIONS
} else {
    ""
}
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val InputBarHeightEstimate: Dp = 72.dp

private val reminderDialogOptions = listOf(
    null to "None",
    5 to "5 minutes before",
    15 to "15 minutes before",
    30 to "30 minutes before",
    60 to "1 hour before",
    120 to "2 hours before",
    240 to "4 hours before",
    720 to "12 hours before",
    1440 to "1 day before",
    2880 to "2 days before",
    4320 to "3 days before",
    7200 to "5 days before",
    10080 to "1 week before"
)

// --- TaskListScreen Composable (REMOVED onNavigateToTaskDetails parameter from signature) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    // REMOVED: onNavigateToTaskDetails: (taskId: Long) -> Unit, <<-- THIS LINE IS GONE
    viewModel: TaskViewModel
) {
    // --- State and Context ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Keep scope
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()

    var newTaskText by remember { mutableStateOf("") }
    var isListeningForSpeech by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showReminderDialog by remember { mutableStateOf(false) }
    var taskToSetReminderFor by remember { mutableStateOf<TaskItem?>(null) }
    var reminderDialogValue by remember { mutableStateOf<Int?>(null) }

    var taskToSetTimeFor by remember { mutableStateOf<TaskItem?>(null) }

    // --- Activity Result Launchers (No Changes) ---
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningForSpeech = false
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { recognizedText ->
                newTaskText = recognizedText
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.speak_to_add))
                }
                speechRecognitionLauncher.launch(intent)
                isListeningForSpeech = true
            } catch (e: Exception) {
                isListeningForSpeech = false
                Log.e("TaskListScreen", "Speech recognition not available or failed.", e)
                Toast.makeText(context, R.string.error_voice_recognition, Toast.LENGTH_SHORT).show()
            }
        } else {
            isListeningForSpeech = false
            Log.w("TaskListScreen", "RECORD_AUDIO permission denied.")
            Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Helper Functions (No Changes) ---
    fun showTimePicker(task: TaskItem) {
        val calendar = Calendar.getInstance()
        val initialHour = task.taskTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = task.taskTime?.minute ?: calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                viewModel.setTaskTime(task.id, selectedTime)
                taskToSetTimeFor = null
            },
            initialHour,
            initialMinute,
            false
        ).apply {
            setButton(TimePickerDialog.BUTTON_NEGATIVE, context.getString(R.string.clear)) { _, _ ->
                viewModel.setTaskTime(task.id, null)
                taskToSetTimeFor = null
            }
            setOnDismissListener {
                if (taskToSetTimeFor == task) {
                    taskToSetTimeFor = null
                }
            }
        }.show()
    }

    // --- Effects (No Changes) ---
    LaunchedEffect(taskToSetTimeFor) {
        taskToSetTimeFor?.let { task ->
            showTimePicker(task)
        }
    }

    // --- Dialogs (No Changes) ---
    if (showReminderDialog) {
        ReminderPickerDialog(
            initialSelection = reminderDialogValue,
            onDismiss = {
                showReminderDialog = false
                taskToSetReminderFor = null
            },
            onSave = { minutes ->
                taskToSetReminderFor?.let { task ->
                    viewModel.setTaskReminder(task.id, minutes)
                }
                showReminderDialog = false
                taskToSetReminderFor = null
            }
        )
    }

    // --- UI Structure ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
    ) {
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = stringResource(R.string.background_image_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Scaffold(
            topBar = {
                TaskListTopAppBar(
                    selectedDate = selectedDate,
                    dateFormatter = dateFormatter,
                    onNavigateToCalendar = onNavigateToCalendar,
                    onNavigateToSettings = onNavigateToSettings
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TaskListContent(
                    tasks = tasksForDate,
                    viewModel = viewModel,
                    onSetReminderClick = { task ->
                        taskToSetReminderFor = task
                        reminderDialogValue = task.reminderMinutesBefore
                        showReminderDialog = true
                    },
                    onCardClick = { task ->
                        taskToSetTimeFor = task
                    }
                    // REMOVED: onCardLongPress lambda from TaskListContent call
                )

                FloatingTaskInputBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    newTaskText = newTaskText,
                    isListeningForSpeech = isListeningForSpeech,
                    onTextChange = { newTaskText = it },
                    onEditClick = {
                        // Placeholder action
                        Log.d("TaskListScreen", "Edit button clicked (placeholder)")
                    },
                    onMicClick = {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onAddTask = {
                        if (newTaskText.isNotBlank()) {
                            val titleToAdd = newTaskText.trim()
                            viewModel.addTask(
                                title = titleToAdd,
                                date = selectedDate,
                                isImportant = false,
                                reminderMinutes = null,
                                taskTime = null
                            )
                            newTaskText = ""
                            keyboardController?.hide()
                        }
                    }
                )
            }
        }
    }
}

// --- TopAppBar Composable (No Changes) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListTopAppBar(
    selectedDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(R.string.tasks_for_date, selectedDate.format(dateFormatter)))
        },
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

// --- TaskListContent Composable (REMOVED onCardLongPress parameter) ---
@Composable
private fun TaskListContent(
    tasks: List<TaskItem>,
    viewModel: TaskViewModel,
    onSetReminderClick: (TaskItem) -> Unit,
    onCardClick: (TaskItem) -> Unit,
    // REMOVED: onCardLongPress: (TaskItem) -> Unit, <<-- THIS LINE IS GONE
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope() // Keep scope

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = InputBarHeightEstimate + 16.dp)
    ) {
        if (tasks.isEmpty()) {
            item {
                EmptyTaskListIndicator(modifier = Modifier.fillParentMaxSize().padding(bottom = InputBarHeightEstimate))
            }
        } else {
            items(items = tasks, key = { task -> task.id }) { task ->
                TaskItemCard(
                    task = task,
                    onDelete = { taskId ->
                        scope.launch { viewModel.deleteTask(taskId) }
                    },
                    reminderMinutes = task.reminderMinutesBefore,
                    taskTime = task.taskTime,
                    isImportant = task.isImportant,
                    onToggleImportant = {
                        scope.launch { viewModel.toggleTaskImportance(task.id) }
                    },
                    onSetReminderClick = { onSetReminderClick(task) },
                    onCardClick = { onCardClick(task) } // Pass task to onCardClick
                    // REMOVED: onCardLongPress lambda from TaskItemCard call
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// --- EmptyTaskListIndicator Composable (No Changes) ---
@Composable
private fun EmptyTaskListIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_tasks_for_date),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// --- FloatingTaskInputBar Composable (No Changes) ---
@Composable
private fun FloatingTaskInputBar(
    modifier: Modifier = Modifier,
    newTaskText: String,
    isListeningForSpeech: Boolean,
    onTextChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onMicClick: () -> Unit,
    onAddTask: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, stringResource(R.string.edit), tint = LocalContentColor.current.copy(alpha = 0.7f))
            }

            OutlinedTextField(
                value = newTaskText,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.add_task_hint)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddTask() })
            )

            IconButton(onClick = onMicClick) {
                Icon(
                    Icons.Default.Mic,
                    stringResource(R.string.accessibility_voice_input),
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onAddTask, enabled = newTaskText.isNotBlank()) {
                Icon(
                    Icons.Default.AddCircle,
                    stringResource(R.string.add_task),
                    tint = if (newTaskText.isNotBlank()) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.38f)
                )
            }
        }
    }
}

// --- ReminderPickerDialog Composable (No Changes) ---
@Composable
fun ReminderPickerDialog(
    initialSelection: Int?,
    onDismiss: () -> Unit,
    onSave: (minutes: Int?) -> Unit
) {
    var selectedOption by remember(initialSelection) { mutableStateOf(initialSelection) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.set_reminder),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                reminderDialogOptions.forEach { (minutes, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = minutes }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == minutes,
                            onClick = { selectedOption = minutes }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(selectedOption) }) { Text(stringResource(R.string.save)) }
                }
            }
        }
    }
}

// --- TaskItemCard Composable (REMOVED onCardLongPress parameter and changed to clickable) ---
// Removed @OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItemCard(
    task: TaskItem,
    onDelete: (Long) -> Unit,
    reminderMinutes: Int?,
    taskTime: LocalTime?,
    isImportant: Boolean,
    onToggleImportant: () -> Unit,
    onSetReminderClick: () -> Unit,
    onCardClick: (TaskItem) -> Unit // Expects TaskItem
    // REMOVED: onCardLongPress: () -> Unit <<-- THIS LINE IS GONE
) {
    val isNotificationActive = reminderMinutes != null || taskTime != null
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(task) } // Use simple clickable, pass task
        , // Removed combinedClickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (taskTime == null) {
                        val message = context.getString(R.string.set_time_first_message)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        onSetReminderClick()
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isNotificationActive) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                    contentDescription = stringResource(R.string.set_reminder_or_time),
                    tint = if (isNotificationActive) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            if (isImportant) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.priority),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp).padding(start = 4.dp, end = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp + 8.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                taskTime?.let {
                    Text(
                        text = it.format(timeFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(onClick = onToggleImportant, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (isImportant) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(if (isImportant) R.string.remove_priority else R.string.set_priority),
                    tint = if (isImportant) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = { onDelete(task.id) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.delete_task),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}