package com.example.taskrabbit.data

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import android.util.Log

// This class holds ALL converters for the database
class Converters {

    // --- LocalDate <-> Long (Epoch Day) Converters ---
    // (Logic taken from your EpochDayConverter)
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return try {
            value?.let { LocalDate.ofEpochDay(it) }
        } catch (e: Exception) {
            Log.e("Converters", "Failed to convert Long $value to LocalDate", e)
            null
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // --- LocalTime <-> Int (Seconds of Day) Converters ---
    // (New logic for the LocalTime field)
    @TypeConverter
    fun fromLocalTimeSeconds(value: Int?): LocalTime? {
        return try {
            value?.let { LocalTime.ofSecondOfDay(it.toLong()) }
        } catch (e: Exception) {
            Log.e("Converters", "Failed to convert Int $value to LocalTime", e)
            null
        }
    }

    @TypeConverter
    fun localTimeToSeconds(time: LocalTime?): Int? {
        return time?.toSecondOfDay()
    }
}