package com.txstate.taskbuddy.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.txstate.taskbuddy.R

class TaskNotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val channelId = "task_channel"

    override fun doWork(): Result {
        val taskName = inputData.getString("task_name")
        val reminderTime = inputData.getString("reminder_time")

        // Log task information
        Log.d("TaskNotificationWorker", "Task Name: $taskName, Reminder Time: $reminderTime")

        // Create notification channel if necessary
        createNotificationChannel()

        // Send the notification
        sendNotification(taskName, reminderTime)

        return Result.success()
    }

    // Create Notification Channel for Android 8.0 (API 26) and above
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Task Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Create the NotificationChannel
            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for task reminder notifications"
            }

            // Register the channel with the system
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    // Function to send the notification
    private fun sendNotification(taskName: String?, reminderTime: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Task Reminder")
            .setContentText("Task: $taskName at $reminderTime")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        // Show the notification
        notificationManager.notify(1, notification) // Notification ID = 1
    }
}
