package com.example.taskrabbit.ui.screens

// <<< ADDED IMPORTS >>>
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast // <<< ADDED: Import Toast
import org.threeten.bp.LocalTime
import androidx.compose.ui.text.style.TextOverflow
// <<< END ADDED IMPORTS >>>

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Keep this, needed for card click
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.taskrabbit.R
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.ui.theme.AppThemeSettings
import com.example.taskrabbit.viewmodel.TaskViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.* // Keep for Calendar and Locale

// --- Constants ---
private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.POST_NOTIFICATIONS
} else {
    "" // No specific permission needed pre-Tiramisu for basic notifications
}
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a") // <<< New Time Formatter
private val InputBarHeightEstimate: Dp = 72.dp
// Reminder options moved outside the composable for performance
private val reminderDialogOptions = listOf(
    null to "None", // Use stringResource(R.string.reminder_option_none) in UI
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
// --- End Constants ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current

    // --- Theme State ---
    val currentThemeSettings by AppThemeSettings.themeSettings.collectAsState()
    val backgroundPainter: Painter? = AppThemeSettings.getBackgroundImagePainter(currentThemeSettings, context)
    val backgroundColor = AppThemeSettings.getBackgroundColor(currentThemeSettings, context)

    // --- ViewModel State ---
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()

    // --- Local UI State ---
    var newTaskText by remember { mutableStateOf("") }
    var isListeningForSpeech by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Reminder Dialog State ---
    var showReminderDialog by remember { mutableStateOf(false) }
    var taskToSetReminderFor by remember { mutableStateOf<TaskItem?>(null) }
    var reminderDialogValue by remember { mutableStateOf<Int?>(null) } // For initial dialog value

    // --- Time Picker State --- // <<< ADDED >>>
    var taskToSetTimeFor by remember { mutableStateOf<TaskItem?>(null) }

    // --- Activity Result Launchers ---
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningForSpeech = false // Always reset listening state
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
                Log.e("TaskListScreen", "Speech recognition not available or failed to launch.", e)
                // TODO: Show user feedback (e.g., Toast) that speech is unavailable
            }
        } else {
            isListeningForSpeech = false
            Log.w("TaskListScreen", "RECORD_AUDIO permission denied.")
            // TODO: Show rationale or message if permission permanently denied
        }
    }
    // --- End Activity Result Launchers ---

    // --- Helper Function to Show Time Picker --- // <<< ADDED >>>
    fun showTimePicker(task: TaskItem) {
        val calendar = Calendar.getInstance()
        // Use task's time if available, otherwise current time
        val initialHour = task.taskTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = task.taskTime?.minute ?: calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // User selected a time and clicked OK
                val selectedTime = LocalTime.of(hourOfDay, minute)
                viewModel.setTaskTime(task.id, selectedTime)
                Log.d("TaskListScreen", "Time selected for task ID ${task.id}: $selectedTime")
                taskToSetTimeFor = null // Reset state after selection
            },
            initialHour,
            initialMinute,
            false // Use 12-hour format (set to true for 24-hour)
        ).apply {
            // Add a "Clear" button
            setButton(TimePickerDialog.BUTTON_NEGATIVE, context.getString(R.string.clear)) { _, _ ->
                // User clicked Clear
                viewModel.setTaskTime(task.id, null) // Set time to null
                Log.d("TaskListScreen", "Time cleared for task ID ${task.id}")
                taskToSetTimeFor = null // Reset state after clearing
            }
            // Handle dismiss explicitly if needed (e.g., user taps outside)
            setOnDismissListener {
                Log.d("TaskListScreen", "Time picker dismissed for task ID ${task.id}")
                // Reset state if the dialog is dismissed without selection/clearing
                if (taskToSetTimeFor == task) { // Avoid resetting if already handled by OK/Clear
                    taskToSetTimeFor = null
                }
            }
        }.show()
    }

    // --- Trigger Time Picker when taskToSetTimeFor changes --- // <<< ADDED >>>
    LaunchedEffect(taskToSetTimeFor) {
        taskToSetTimeFor?.let { task ->
            showTimePicker(task)
        }
    }
    // --- End Time Picker Logic ---

    // --- Reminder Dialog Composable ---
    // Only composed when showReminderDialog is true
    if (showReminderDialog) {
        ReminderPickerDialog(
            initialSelection = reminderDialogValue,
            onDismiss = {
                showReminderDialog = false
                taskToSetReminderFor = null // Reset selected task
            },
            onSave = { minutes ->
                taskToSetReminderFor?.let { task ->
                    viewModel.setTaskReminder(task.id, minutes)
                    Log.d("TaskListScreen", "Saved reminder ($minutes min) for task ID: ${task.id}")
                }
                showReminderDialog = false
                taskToSetReminderFor = null // Reset selected task
                // TODO: Potentially request notification permission here if setting a reminder and permission not granted
            }
        )
    }
    // --- End Reminder Dialog Composable ---

    // --- Main UI Structure ---
    Box( // Root Box for background
        modifier = Modifier
            .fillMaxSize()
            .background(if (backgroundPainter == null) backgroundColor else Color.Transparent)
    ) {
        // Background Image Layer
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = stringResource(R.string.background_image_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Scaffold Layer (Top Bar, Content)
        Scaffold(
            topBar = {
                TaskListTopAppBar(
                    selectedDate = selectedDate,
                    dateFormatter = dateFormatter, // Pass formatter
                    onNavigateToCalendar = onNavigateToCalendar,
                    onNavigateToSettings = onNavigateToSettings
                )
            },
            containerColor = Color.Transparent // Make Scaffold background transparent
        ) { paddingValues ->

            // Content Box (Task List + Input Bar)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply Scaffold padding
            ) {
                // Task List
                TaskListContent(
                    tasks = tasksForDate,
                    viewModel = viewModel, // Pass for actions
                    onSetReminderClick = { task -> // Lambda to handle reminder icon click
                        // This is now only called IF time is set (logic moved to TaskItemCard)
                        taskToSetReminderFor = task
                        reminderDialogValue = task.reminderMinutesBefore
                        showReminderDialog = true
                        Log.d("TaskListScreen", "Opening reminder dialog for task ID: ${task.id}")
                    },
                    onCardClick = { task -> // Lambda to handle card click
                        taskToSetTimeFor = task // Set state to trigger LaunchedEffect
                        Log.d("TaskListScreen", "Card clicked for task ID: ${task.id}, preparing time picker.")
                    }
                )

                // Floating Input Bar (Aligned to Bottom)
                FloatingTaskInputBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    newTaskText = newTaskText,
                    isListeningForSpeech = isListeningForSpeech,
                    onTextChange = { newTaskText = it },
                    onEditClick = {
                        Log.d("TaskListScreen", "Edit button clicked (placeholder)")
                        // TODO: Implement edit action
                    },
                    onMicClick = {
                        // Request audio permission before launching speech recognizer
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onAddTask = {
                        if (newTaskText.isNotBlank()) {
                            viewModel.addTask(
                                title = newTaskText,
                                date = selectedDate,
                                isImportant = false,
                                reminderMinutes = null,
                                taskTime = null
                            )
                            newTaskText = "" // Clear input
                            keyboardController?.hide() // Hide keyboard
                        }
                    }
                )
            } // End Content Box
        } // End Scaffold
    } // End Root Box
}

// --- Extracted TopAppBar ---
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

// --- Extracted Task List Content ---
@Composable
private fun TaskListContent(
    tasks: List<TaskItem>,
    viewModel: TaskViewModel,
    onSetReminderClick: (TaskItem) -> Unit, // Callback for reminder icon (if time is set)
    onCardClick: (TaskItem) -> Unit, // Callback for card itself
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = InputBarHeightEstimate + 16.dp) // Padding for floating input bar
    ) {
        if (tasks.isEmpty()) {
            item {
                EmptyTaskListIndicator(modifier = Modifier.fillParentMaxSize().padding(bottom = InputBarHeightEstimate))
            }
        } else {
            items(items = tasks, key = { task -> task.id }) { task ->
                TaskItemCard(
                    task = task,
                    onDelete = { viewModel.deleteTask(task.id) },
                    reminderMinutes = task.reminderMinutesBefore,
                    taskTime = task.taskTime,
                    isImportant = task.isImportant,
                    onToggleImportant = { viewModel.toggleTaskImportance(task.id) },
                    // Pass the callback, logic to call it is now inside TaskItemCard
                    onSetReminderClick = { onSetReminderClick(task) },
                    onCardClick = { onCardClick(task) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// --- Extracted Empty State Indicator ---
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


// --- Floating Input Bar Composable (Simplified) ---
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
            // Edit Button
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, stringResource(R.string.edit), tint = LocalContentColor.current.copy(alpha = 0.7f))
            }

            // Task Input Field
            OutlinedTextField(
                value = newTaskText,
                onValueChange = onTextChange,
                placeholder = { Text(stringResource(R.string.add_task_hint)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors( // Transparent background
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
                keyboardActions = KeyboardActions(onDone = { onAddTask() }) // Trigger add on keyboard 'Done'
            )

            // Mic Button
            IconButton(onClick = onMicClick) {
                Icon(
                    Icons.Default.Mic,
                    stringResource(R.string.accessibility_voice_input),
                    tint = if (isListeningForSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            // Add Button
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

// --- Reminder Picker Dialog Composable (Uses constant options) ---
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
                    .verticalScroll(rememberScrollState()) // Allow scrolling
            ) {
                // Title
                Text(
                    text = stringResource(R.string.set_reminder),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Options (using the constant list)
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
                        // TODO: Replace hardcoded labels with string resources for localization
                        // Text(stringResource(getReminderLabelResId(minutes)))
                        Text(label)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Buttons
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

// --- Task Item Card Composable (MODIFIED) ---
@Composable
fun TaskItemCard(
    task: TaskItem,
    onDelete: (Long) -> Unit,
    reminderMinutes: Int?,
    taskTime: LocalTime?,
    isImportant: Boolean,
    onToggleImportant: () -> Unit,
    onSetReminderClick: () -> Unit, // This callback is now conditional
    onCardClick: () -> Unit
) {
    val isNotificationActive = reminderMinutes != null || taskTime != null
    val context = LocalContext.current // <<< Get context for Toast

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
            // Reminder/Time Button
            IconButton(
                onClick = {
                    // <<< MODIFIED LOGIC >>>
                    if (taskTime == null) {
                        // Time not set, show Toast message
                        val message = context.getString(R.string.set_time_first_message) // <<< Use new string resource
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        Log.d("TaskItemCard", "Reminder click blocked for task ID ${task.id}: Time not set.")
                    } else {
                        // Time is set, call the original callback to open reminder dialog
                        onSetReminderClick()
                        Log.d("TaskItemCard", "Reminder click allowed for task ID ${task.id}: Opening dialog.")
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isNotificationActive) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                    contentDescription = stringResource(R.string.set_reminder_or_time),
                    tint = if (isNotificationActive) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.7f)
                    // Icon appearance remains based on isNotificationActive
                )
            }

            // Importance Indicator (if important)
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

            // Task Title and Time in a Column
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

            // Toggle Importance Button
            IconButton(onClick = onToggleImportant, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (isImportant) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(if (isImportant) R.string.remove_priority else R.string.set_priority),
                    tint = if (isImportant) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            // Delete Button
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

// --- Remember to Add String Resources ---
// Add these to your res/values/strings.xml and res/values-et/strings.xml:
// <string name="set_time_first_message">Set time first</string>