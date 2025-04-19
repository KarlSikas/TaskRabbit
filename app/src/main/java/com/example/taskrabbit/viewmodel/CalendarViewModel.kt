package com.example.taskrabbit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDate

class CalendarViewModel : ViewModel() {
    private val _dates = MutableLiveData<List<LocalDate>>()
    val dates: LiveData<List<LocalDate>> = _dates

    init {
        loadDates()
    }

    private fun loadDates() {
        val today = LocalDate.now()
        val twoYears = 730L

        val futureDates = (1..twoYears).map { today.plusDays(it) }
        val pastDates = (1..twoYears).map { today.minusDays(it) }

        val sortedDates = listOf(today) + futureDates + pastDates.reversed()

        _dates.value = sortedDates
    }
}
