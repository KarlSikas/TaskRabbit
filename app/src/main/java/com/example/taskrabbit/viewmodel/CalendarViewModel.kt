package com.example.taskrabbit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData // Needed for Flow -> LiveData conversion
import androidx.lifecycle.viewModelScope
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.TaskDao
import com.example.taskrabbit.data.TaskItem
import kotlinx.coroutines.flow.map // Needed for Flow transformations
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao: TaskDao

    // LiveData for the dates to display in the calendar grid (+/- 2 years)
    private val _calendarGridDates = MutableLiveData<List<LocalDate>>()
    val calendarGridDates: LiveData<List<LocalDate>> = _calendarGridDates

    // LiveData containing the set of dates that have tasks, obtained from the DAO
    val datesWithTasks: LiveData<Set<LocalDate>>

    init {
        taskDao = AppDatabase.getDatabase(application).taskDao()

        generateCalendarGridDates()

        // Use the DAO's getDatesWithTasks() Flow, map List to Set, convert to LiveData
        datesWithTasks = taskDao.getDatesWithTasks() // Assumes this returns Flow<List<LocalDate>>
            .map { dateList -> dateList.toSet() } // Transform List to Set
            .asLiveData() // Convert Flow to LiveData
    }

    // Generates the static list of dates for the calendar grid display
    private fun generateCalendarGridDates() {
        val today = LocalDate.now()
        val twoYearsInDays = 730L // Approx 2 years
        val startDate = today.minusDays(twoYearsInDays)
        val endDate = today.plusDays(twoYearsInDays)

        val datesList = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            datesList.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        _calendarGridDates.value = datesList // Update the LiveData for the grid
    }

    // Function to add a new task using the DAO
    fun addTask(task: TaskItem) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    // Function to get tasks for a specific selected date (returns LiveData)
    fun getTasksForDay(date: LocalDate): LiveData<List<TaskItem>> {
        return taskDao.getTasksForDate(date).asLiveData()
    }

    // --- Add other functions as needed to interact with taskDao ---

    // Example: Update a task
    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    // Example: Delete a task
    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    // Example: Get a single task by ID (useful for editing)
    // Note: This is suspend, so call it from a coroutine if needed outside the ViewModel
    suspend fun getTaskById(taskId: Long): TaskItem? {
        return taskDao.getTaskById(taskId)
    }
}