package com.example.taskrabbit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime // <<< Import LocalTime

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isImportant: Boolean = false,
    val dueDate: LocalDate? = null,
    val reminderMinutesBefore: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val taskTime: LocalTime? = null // <<< New field for specific time (nullable)
)