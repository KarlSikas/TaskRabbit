package com.example.taskrabbit.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
// import org.threeten.bp.LocalDate // This import isn't actually used here, but doesn't hurt

@Entity(tableName = "calendar_dates")
data class CalendarDateEntity(
    // Store LocalDate as epoch days for efficient querying and sorting
    @PrimaryKey // Good, this will be the unique ID for each date entry
    @ColumnInfo(name = "date_epoch_day") // Explicit column name is good practice
    val dateEpochDay: Long // Correct type (Long) to store epoch days
)
