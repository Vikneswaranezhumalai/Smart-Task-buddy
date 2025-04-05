package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
class RecommendationFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private val isProcessingState = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setContent {
                val isProcessing by isProcessingState
                Column(modifier = Modifier.fillMaxSize()) {
                    CommonToolbar(
                        title = "Smart AI Recommendations",
                        navigationIcon = Icons.Default.ArrowBack,
                        onBackButtonClick = {
                            requireActivity().supportFragmentManager.popBackStack()
                        },
                        onCompletedTasksClick = null
                    )

                    RecommendationScreen(taskViewModel)
                }
                if (isProcessing) {
                    println("Voice Input Received: isProcessingState true")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    println("Loading state: isProcessingState false")
                }
            }
        }
    }

    @Composable
    fun RecommendationScreen(taskViewModel: TaskViewModel) {
        val context = LocalContext.current
        val recommendations by taskViewModel.recommendedTasks.collectAsState()
        val error by taskViewModel.errorMessage.collectAsState()

        // Show loader immediately on screen load
        LaunchedEffect(Unit) {
            isProcessingState.value = true
            taskViewModel.loadRecommendations()
        }

        // Hide loader once data is loaded
        LaunchedEffect(recommendations) {
            if (recommendations.isNotEmpty()) {
                isProcessingState.value = false
            }
        }

        // Button to refresh
        Button(
            onClick = {
                isProcessingState.value = true
                taskViewModel.loadRecommendations()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Refresh Recommendations")
        }

        // Show list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(recommendations) { suggestion ->
                RecommendationCard(suggestion) {
                    val loggedInUser = taskViewModel.getLoggedInUser()
                    val newTask = Task(
                        userId = loggedInUser?.id ?: 0,
                        taskName = suggestion.taskName,
                        description = "Added from AI suggestion",
                        priority = suggestion.priority,
                        category = suggestion.category,
                        dueDate = suggestion.dueDate,
                        reminderTime = suggestion.reminderTime
                    )
                    taskViewModel.addTask(context, newTask)
                    Toast.makeText(context, "Task added from AI Suggestions!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()

                }
            }
        }
    }


    @Composable
    fun RecommendationCard(task: Task, onAdd: () -> Unit) {
        Card(
            backgroundColor = Color(0xFFF8F8F8),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(task.taskName, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Category: ${task.category}", style = MaterialTheme.typography.body2)
                Text("Suggested Time: ${task.reminderTime}", style = MaterialTheme.typography.body2)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAdd,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add to My Tasks")
                }
            }
        }
    }
}
