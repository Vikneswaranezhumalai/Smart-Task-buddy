package com.txstate.taskbuddy


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.TaskRepository
import com.txstate.taskbuddy.notifications.TaskNotificationWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    // Room Database Instance
    private val taskRepository: TaskRepository
    val getAllTasks: LiveData<List<Task>>
    val getCompletedTasks: LiveData<List<Task>>

    init {
        val taskDatabase = TaskDatabase.getDatabase(application).taskDao()
        taskRepository = TaskRepository(taskDatabase)
        getAllTasks = taskRepository.getAllTasks()
        getCompletedTasks = taskRepository.getCompletedTask()
    }

    fun addTask(context: Context, task: Task) {
        // Insert task into Room database
        GlobalScope.launch {
            // Insert into database using Room (assuming taskDatabase is initialized)
            taskRepository.insertTask(task)
        }
        // Schedule notification using WorkManager
        scheduleTaskNotification(context, task)
    }

    // LiveData for a single task
    fun getTaskById(taskId: Int): LiveData<Task> {
        return taskRepository.getTaskById(taskId)
    }

    fun updateTaskStatus(task: Task) {
        // Update task status in the database (e.g., mark it as completed)
        GlobalScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        // Delete the task from the database
        GlobalScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    private fun scheduleTaskNotification(context: Context, task: Task) {
        val inputData = workDataOf(
            "task_name" to task.taskName,
            "reminder_time" to task.reminderTime,
            "dueDate" to task.dueDate
        )

        // Calculate the delay based on the reminder date and time
        val delayInMillis = calculateDelay(task.dueDate, task.reminderTime)

        val notificationRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(5000, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(notificationRequest)
    }

    private fun calculateDelay(dueDate: String, reminderTime: String): Long {
        // Get the current time
        val currentTime = Calendar.getInstance()

        // Prepare SimpleDateFormat for both the date and time formats
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // e.g., "2025-01-30"
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // e.g., "10:00"

        // Parse the reminder date and time
        val reminderDate: Date = dateFormat.parse(dueDate) ?: return 0L
        val reminderTime: Date = timeFormat.parse(reminderTime) ?: return 0L

        // Set the reminder date and time in a Calendar object
        val reminderCalendar = Calendar.getInstance().apply {
            time = reminderDate
            set(Calendar.HOUR_OF_DAY, reminderTime.hours)
            set(Calendar.MINUTE, reminderTime.minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Calculate the delay (in milliseconds) from the current time
        val delayInMillis = reminderCalendar.timeInMillis - currentTime.timeInMillis

        // If the calculated delay is negative (i.e., the reminder is in the past), add 24 hours to it
        return if (delayInMillis < 0) {
            delayInMillis + TimeUnit.DAYS.toMillis(1)
        } else {
            delayInMillis
        }
    }


}
