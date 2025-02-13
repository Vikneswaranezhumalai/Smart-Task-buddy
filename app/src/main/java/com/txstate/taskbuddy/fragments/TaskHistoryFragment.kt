package com.txstate.taskbuddy.fragments
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar


class TaskHistoryFragment : Fragment(){
    private lateinit var taskViewModel: TaskViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        return ComposeView(requireContext()).apply {
            setContent {
                TaskHistoryFragment()
            }
        }
    }

    @SuppressLint("NotConstructor")
    @Composable
     fun TaskHistoryFragment() {
        val taskLists by taskViewModel.getCompletedTasks.observeAsState(initial = emptyList())
        Surface(modifier = Modifier.fillMaxHeight()) {
            Column {
                // Toolbar
                CommonToolbar(
                    title = "Completed Task History",
                    navigationIcon = Icons.Default.ArrowBack,
                    onBackButtonClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                )
                // Task List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(taskLists) { task ->
                        TaskHistoryItem(task = task) {
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TaskHistoryItem(task: Task, onClick: (Task) -> Unit) {

        val priorityColor = when (task.priority) {
            "Completed" -> Color.Green
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

                // Priority
                Text(
                    text = "Priority: ${task.priority}",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    color = priorityColor
                )

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