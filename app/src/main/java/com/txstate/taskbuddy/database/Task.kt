package com.txstate.taskbuddy.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskName: String,
    val description: String,
    val priority: String,
    val category: String,  // Added category field
    val dueDate: String,
    val reminderTime: String
)
