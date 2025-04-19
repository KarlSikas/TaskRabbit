package com.example.taskrabbit.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val _tasksForSelectedDate = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksForSelectedDate: StateFlow<List<TaskItem>> = _tasksForSelectedDate.asStateFlow()

    fun loadTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            taskDao.getTasksForDate(date)
                .flowOn(Dispatchers.IO)
                .collect { tasks ->
                    _tasksForSelectedDate.value = tasks
                }
        }
    }

    fun addTask(title: String, date: LocalDate?) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskItem(title = title, dueDate = date)
            taskDao.insertTask(task)
            //loadTasksForDate(date ?: LocalDate.now()) // Refresh tasks
        }
    }

    fun toggleTaskImportance(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId.toLong()) ?: return@launch
            val updatedTask = task.copy(isImportant = !task.isImportant)
            taskDao.updateTask(updatedTask)
            //loadTasksForDate(task.dueDate ?: LocalDate.now()) // Refresh tasks
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId) ?: return@launch
            taskDao.deleteTask(task)
            // loadTasksForDate(task.dueDate ?: LocalDate.now()) // Refresh tasks
        }
    }

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