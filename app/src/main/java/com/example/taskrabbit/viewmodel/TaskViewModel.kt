package com.example.taskrabbit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.data.TaskDao
// --- Import the ReminderScheduler ---
import com.example.taskrabbit.util.ReminderScheduler
// --- End Import ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    // --- Instantiate the ReminderScheduler ---
    private val reminderScheduler = ReminderScheduler(application)
    // --- End Instantiation ---

    // --- StateFlow for selected date ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // --- Reactive StateFlow for tasks ---
    val tasksForSelectedDate: StateFlow<List<TaskItem>> = _selectedDate.flatMapLatest { date ->
        Log.d("TaskViewModel", "flatMapLatest triggered for date: $date. Getting DAO Flow.")
        taskDao.getTasksForDate(date)
            .catch { e ->
                Log.e("TaskViewModel", "Error in tasksForSelectedDate flow for date $date", e)
                emit(emptyList())
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Calendar related functions ---
    fun hasPriorityTaskOnDate(date: LocalDate): Flow<Boolean> {
        return taskDao.getPriorityTaskCountOnDate(date)
            .map { count -> count > 0 }
            .catch { e ->
                Log.e("TaskViewModel", "Error in hasPriorityTaskOnDate flow for date $date", e)
                emit(false)
            }
    }

    fun getPriorityTaskTitlesOnDate(date: LocalDate): Flow<List<String>> {
        return taskDao.getPriorityTaskTitlesForDate(date)
            .catch { e ->
                Log.e("TaskViewModel", "Error in getPriorityTaskTitlesOnDate flow for date $date", e)
                emit(emptyList())
            }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        Log.d("TaskViewModel", "Date selected: $date")
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
            Log.d("TaskViewModel", "Task added: '$title', Date: $dateToUse, Time: $taskTime, ID: $newTaskId")

            val insertedTask = task.copy(id = newTaskId)

            // --- Schedule Reminder (if applicable) ---
            // No need to check reminderMinutes/taskTime here, schedule() does it
            reminderScheduler.schedule(insertedTask) // <<< USE SCHEDULER
            // --- End Scheduling ---
        }
    }

    fun toggleTaskImportance(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(isImportant = !task.isImportant)
                taskDao.updateTask(updatedTask)
                Log.d("TaskViewModel", "Toggled importance for task ID: $taskId")
            } else {
                Log.e("TaskViewModel", "toggleTaskImportance called for non-existent task ID: $taskId")
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
                    Log.d("TaskViewModel", "Database update called for task ID: $taskId with reminder: $reminderMinutes")

                    // --- Schedule or Cancel Reminder ---
                    reminderScheduler.schedule(updatedTask) // <<< USE SCHEDULER (handles both schedule/cancel logic internally based on fields)
                    // --- End Scheduling/Cancellation ---

                } else {
                    Log.d("TaskViewModel", "Reminder for task ID: $taskId not changed. No DB update or scheduling needed.")
                }
            } else {
                Log.e("TaskViewModel", "setTaskReminder called for non-existent task ID: $taskId")
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId) // Fetch task first to ensure it exists
            if (task != null) {
                // --- Cancel Reminder FIRST ---
                reminderScheduler.cancel(taskId) // <<< USE SCHEDULER
                // --- End Cancellation ---

                taskDao.deleteTask(task) // Then delete from DB
                Log.d("TaskViewModel", "Deleted task ID: $taskId after cancelling reminder.")

            } else {
                Log.e("TaskViewModel", "deleteTask called for non-existent task ID: $taskId")
            }
        }
    }

    fun setTaskTime(taskId: Long, time: LocalTime?) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task == null) {
                Log.e("TaskViewModel", "setTaskTime called for non-existent task ID: $taskId")
                return@launch
            }
            if (task.taskTime != time) {
                val updatedTask = task.copy(taskTime = time)
                taskDao.updateTask(updatedTask)
                Log.d("TaskViewModel", "Set time for task ID: $taskId to $time")

                // --- Reschedule or Cancel Reminder based on new time ---
                reminderScheduler.schedule(updatedTask) // <<< USE SCHEDULER (handles reschedule/cancel logic internally)
                // --- End Rescheduling/Cancellation ---

            } else {
                Log.d("TaskViewModel", "Time for task ID: $taskId already set to $time. No update or scheduling needed.")
            }
        }
    }

    // --- ViewModel Factory ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}