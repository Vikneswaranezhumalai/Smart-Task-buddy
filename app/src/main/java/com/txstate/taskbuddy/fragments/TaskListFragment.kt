package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.notifications.TaskNotificationWorker
import java.util.concurrent.TimeUnit


class TaskListFragment : Fragment(){
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        // Initialize AuthManager with UserRepository
        val userRepository =
            UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
        authManager = AuthManager(requireContext(), userRepository)


        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
        // Insert dummy tasks by passing the context
       // taskViewModel.insertDummyTasks(requireContext()) // Pass context to the ViewModel
        return ComposeView(requireContext()).apply {
            setContent {
                    TaskListFragmentContent()
            }
        }
    }
    // Handle the result of permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show notifications
                val inputData = workDataOf(
                    "task_name" to "Task Reminder, You have a task to complete",
                    "reminder_time" to "",
                    "dueDate" to ""
                )
                val notificationRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                    .setInputData(inputData)
                    .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(requireContext()).enqueue(notificationRequest)
            }
        }
    }
    @Composable
    @Preview(showBackground = true)
    fun PreviewTaskListFragmentContent() {
        TaskListFragmentContent()
    }

    @Composable
    fun TaskListFragmentContent() {
        val taskList by taskViewModel.getAllTasks.observeAsState(initial = emptyList())
        val user = authManager.getLoggedInUser()
        Surface(modifier = Modifier.fillMaxHeight()) {
            Column {
                // Toolbar
                CommonToolbar(
                    title = "Hey " + user?.name,
                    onBackButtonClick = null,
                    onCompletedTasksClick =null,
                    onSettingsClick = {
                        // Navigate to Completed Task History Screen
                        val settingFragment = SettingFragment()
                        val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(containerId, settingFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                )

                // Task List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(taskList) { task ->
                        TaskItem(task = task) {
                            // Handle task click
                            val taskId = task.id  // The task ID you want to pass
                            val taskDetailsFragment = TaskDetailsFragment()
                            val bundle = Bundle().apply {
                                putInt("TASK_ID", taskId)  // Key-value pair for passing task ID
                            }
                            taskDetailsFragment.arguments = bundle
                            val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(containerId, taskDetailsFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }

                // Add Task Button
                Button(
                    onClick = {
                        val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(containerId, AddTaskFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Add Task")
                }
            }
        }
    }


    @Composable
    fun TaskItem(task: Task, onClick: (Task) -> Unit) {

        val priorityColor = when (task.priority) {
            "High" -> Color.Red
            "Medium" -> Color(0xFFFFA500) // Orange color
            "Low" -> Color.Green
            else -> Color.Gray // Default color if not defined
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick(task) },
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Task Name
                Text(text = task.taskName, style = MaterialTheme.typography.h6)

                // Description
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                // Priority & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Priority: ${task.priority}",
                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                        color = priorityColor
                    )
                    Text(text = "Category: ${task.category}", style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                }




                // Due Date and Reminder Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Due: ${task.dueDate}",style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Reminder: ${task.reminderTime}", style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }


}