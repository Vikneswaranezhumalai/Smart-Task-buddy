package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import androidx.compose.ui.Alignment
import androidx.compose.material.Text
import androidx.compose.ui.text.font.FontWeight
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar

class TaskDetailsFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private var taskId: Int = 0  // Assume you pass this from the previous screen or Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        // Get task ID passed as an argument
        taskId = arguments?.getInt("TASK_ID") ?: 0

        // Return ComposeView and setContent inside it
        return ComposeView(requireContext()).apply {
            setContent {
                // Observe task LiveData and use it in Composable
                val task by taskViewModel.getTaskById(taskId).observeAsState()
                task?.let {
                    Column {
                        CommonToolbar(
                            title = "View Task",
                            navigationIcon = Icons.Default.ArrowBack,
                            onBackButtonClick = {
                                requireActivity().supportFragmentManager.popBackStack()
                            },
                            onCompletedTasksClick = null
                        )
                        TaskDetailsFragmentContent(
                            task = it,
                            onMarkCompleted = { onMarkCompleted(it) },
                            onDelete = { onDelete(it) }
                        )
                    }

                } ?: run {
                    // Show a loading state or empty content while the task is being fetched
//                    Text(
//                        text = "Loading task details...",
//                       // modifier = Modifier.align(Alignment.Center)  // This centers the text
//                    )
                }
            }
        }
    }

    private fun onMarkCompleted(task: Task) {
        val updatedTask = task.copy(priority = "Completed")  // Set priority as "Completed"
        taskViewModel.updateTaskStatus(updatedTask)
        requireActivity().supportFragmentManager.popBackStack()
        Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show()
    }

    private fun onDelete(task: Task) {
        taskViewModel.deleteTask(task)
        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack() // Go back after deletion
    }

    @Composable
    fun TaskDetailsFragmentContent(
        task: Task,
        onMarkCompleted: (Task) -> Unit,
        onDelete: (Task) -> Unit
    ) {
        val priorityColor = when (task.priority) {
            "High" -> Color.Red
            "Medium" -> Color(0xFFFFA500) // Orange color
            "Low" -> Color.Green
            else -> Color.Gray // Default color if not defined
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Task Title
            Text(
                text = task.taskName,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.body2)
                Text(text = "Reminder: ${task.reminderTime}", style = MaterialTheme.typography.body2)
            }

            // Buttons: Mark as Completed and Delete
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onMarkCompleted(task) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Text("Mark as Completed", color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onDelete(task) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}


