package com.example.taskrabbit.viewmodel

import android.app.Application
import android.util.Log // Keep Log import for error logging
import androidx.lifecycle.*
import com.example.taskrabbit.TaskRabbitApplication
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.data.TaskDao
import com.example.taskrabbit.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.lang.IllegalArgumentException
import java.lang.RuntimeException

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // --- Properties ---
    private lateinit var taskDao: TaskDao
    private lateinit var reminderScheduler: ReminderScheduler

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    lateinit var tasksForSelectedDate: StateFlow<List<TaskItem>>

    init {
        try {
            val localApplication = getApplication<TaskRabbitApplication>()

            reminderScheduler = ReminderScheduler(localApplication)

            // Access the database via the Application class property
            taskDao = localApplication.database.taskDao()

            // Initialize tasksForSelectedDate StateFlow AFTER taskDao is ready
            tasksForSelectedDate = _selectedDate.flatMapLatest { date ->
                // Log removed: "TASK_FLOW: flatMapLatest triggered for date: $date."
                taskDao.getTasksForDate(date)
                    .catch { e ->
                        Log.e("TaskViewModel", "TASK_FLOW: Error in getTasksForDate flow for date $date", e) // Keep error log
                        emit(emptyList())
                    }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        } catch (e: Exception) {
            Log.e("TaskViewModel", "INIT: CRITICAL ERROR during TaskViewModel initialization!", e) // Keep error log
            // Handle the error
            if (!::tasksForSelectedDate.isInitialized) {
                // Log removed: "INIT: tasksForSelectedDate not initialized due to error, setting empty."
                tasksForSelectedDate = MutableStateFlow(emptyList())
            }
            throw RuntimeException("Failed to initialize TaskViewModel due to: ${e.message}", e)
        }
        // Log removed: "INIT: TaskViewModel initialization finished."
    }


    // --- Calendar related functions ---
    fun hasPriorityTaskOnDate(date: LocalDate): Flow<Boolean> {
        val flow = taskDao.getPriorityTaskCountOnDate(date)
            .map { count -> count > 0 }
            .catch { e ->
                Log.e("TaskViewModel", "Error in hasPriorityTaskOnDate flow for date $date", e) // Keep error log
                emit(false)
            }
        return flow
    }

    fun getPriorityTaskTitlesOnDate(date: LocalDate): Flow<List<String>> {
        val flow = taskDao.getPriorityTaskTitlesForDate(date)
            .catch { e ->
                Log.e("TaskViewModel", "Error in getPriorityTaskTitlesOnDate flow for date $date", e) // Keep error log
                emit(emptyList())
            }
        return flow
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // Log removed: "ACTION: Date selected: $date"
    }

    // --- Task modification functions ---

    fun addTask(
        title: String,
        date: LocalDate?,
        isImportant: Boolean,
        reminderMinutes: Int?,
        taskTime: LocalTime?
    ) {
        val dateToUse = date ?: _selectedDate.value
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskItem(
                title = title,
                dueDate = dateToUse,
                isImportant = isImportant,
                reminderMinutesBefore = reminderMinutes,
                taskTime = taskTime
            )
            val newTaskId = taskDao.insertTask(task)
            // Log removed: "ACTION: Task added: '$title', Date: $dateToUse, Time: $taskTime, ID: $newTaskId"

            val insertedTask = task.copy(id = newTaskId)
            reminderScheduler.schedule(insertedTask)
        }
    }

    fun toggleTaskImportance(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(isImportant = !task.isImportant)
                taskDao.updateTask(updatedTask)
                // Log removed: "ACTION: Toggled importance for task ID: $taskId to ${updatedTask.isImportant}"
            } else {
                Log.e("TaskViewModel", "ACTION: toggleTaskImportance failed. Task ID not found: $taskId") // Keep error log
            }
        }
    }

    fun setTaskReminder(taskId: Long, reminderMinutes: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                if (task.reminderMinutesBefore != reminderMinutes) {
                    val updatedTask = task.copy(reminderMinutesBefore = reminderMinutes)
                    taskDao.updateTask(updatedTask)
                    // Log removed: "ACTION: Updated reminder for task ID: $taskId to $reminderMinutes minutes."
                    reminderScheduler.schedule(updatedTask)
                } else {
                    // Log removed: "ACTION: Reminder for task ID: $taskId already set to $reminderMinutes. No change."
                }
            } else {
                Log.e("TaskViewModel", "ACTION: setTaskReminder failed. Task ID not found: $taskId") // Keep error log
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                reminderScheduler.cancel(taskId)
                taskDao.deleteTask(task)
                // Log removed: "ACTION: Deleted task ID: $taskId after cancelling reminder."
            } else {
                Log.e("TaskViewModel", "ACTION: deleteTask failed. Task ID not found: $taskId") // Keep error log
            }
        }
    }

    fun setTaskTime(taskId: Long, time: LocalTime?) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task == null) {
                Log.e("TaskViewModel", "ACTION: setTaskTime failed. Task ID not found: $taskId") // Keep error log
                return@launch
            }
            if (task.taskTime != time) {
                val updatedTask = task.copy(taskTime = time)
                taskDao.updateTask(updatedTask)
                // Log removed: "ACTION: Set time for task ID: $taskId to $time"
                reminderScheduler.schedule(updatedTask)
            } else {
                // Log removed: "ACTION: Time for task ID: $taskId already set to $time. No change."
            }
        }
    }

    // --- ViewModel Factory ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        // Log removed: Factory init log
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Log removed: Factory create call log
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                // Log removed: Factory creating instance log
                return TaskViewModel(application) as T
            }
            // Log removed: Factory error log (replaced by throwing exception)
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}") // Added class name to exception
        }
    }
}