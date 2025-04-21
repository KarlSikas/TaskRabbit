package com.example.taskrabbit.data.repository

import androidx.lifecycle.LiveData
// Removed: import androidx.lifecycle.map
import com.example.taskrabbit.db.dao.CalendarDateDao          // Needs the updated DAO
import com.example.taskrabbit.db.entity.CalendarDateEntity
// Removed: import com.example.taskrabbit.db.converter.EpochDayConverter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class CalendarRepository(private val calendarDateDao: CalendarDateDao) {

    private val twoYearsDays = 730L

    // This now correctly expects LiveData<List<LocalDate>> from the DAO
    val allDates: LiveData<List<LocalDate>> = calendarDateDao.getAllDates()

    suspend fun refreshDatesIfNeeded() {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now()
            val minDate = today.minusDays(twoYearsDays)
            val maxDate = today.plusDays(twoYearsDays)

            calendarDateDao.deleteOlderThan(minDate.toEpochDay())

            val requiredDates = mutableListOf<LocalDate>()
            var currentDate = minDate
            while (!currentDate.isAfter(maxDate)) {
                requiredDates.add(currentDate)
                currentDate = currentDate.plusDays(1)
            }

            val dateEntities = requiredDates.map { CalendarDateEntity(it.toEpochDay()) }
            calendarDateDao.insertDates(dateEntities)
        }
    }
}