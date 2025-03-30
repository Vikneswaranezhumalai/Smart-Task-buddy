package com.txstate.taskbuddy.database

import kotlinx.serialization.Serializable

@Serializable
data class ExtractedTask(
    val taskName: String,
    val description: String,
    val category: String = "General",
    val priority: String = "Medium",
    val dueDate: String,
    val reminderTime: String
)
