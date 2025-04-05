package com.txstate.taskbuddy.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.notifications.TaskNotificationWorker
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskListFragment : Fragment() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var authManager: AuthManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        val userRepository = UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
        authManager = AuthManager(requireContext(), userRepository)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                TaskListFragmentContent()
            }
        }
    }

    @Composable
    fun TaskListFragmentContent() {
        val taskList by taskViewModel.getAllTasks.observeAsState(initial = emptyList())
        val user = authManager.getLoggedInUser()

        var selectedPriority by remember { mutableStateOf("All") }
        var selectedCategory by remember { mutableStateOf("All") }
        var selectedDue by remember { mutableStateOf("All") }
        var showFilterDialog by remember { mutableStateOf(false) }

        val filteredTasks = taskList.filter { task ->
            val priorityMatch = selectedPriority == "All" || task.priority == selectedPriority
            val categoryMatch = selectedCategory == "All" || task.category == selectedCategory
            val dueMatch = when (selectedDue) {
                "All" -> true
                "Today" -> task.dueDate == getTodayDate()
                "This Week" -> isDueThisWeek(task.dueDate)
                "Overdue" -> isOverdue(task.dueDate)
                else -> true
            }
            priorityMatch && categoryMatch && dueMatch
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                CommonToolbar(
                    title = "Hey ${user?.name ?: "User"}",
                    onBackButtonClick = null,
                    onCompletedTasksClick = null,
                    onSettingsClick = {
                        val settingFragment = SettingFragment()
                        val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(containerId, settingFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                )

                QuickSuggestionBanner {
                    val recommendationFragment = RecommendationFragment()
                    val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(containerId, recommendationFragment)
                        .addToBackStack(null)
                        .commit()
                }

                if (showFilterDialog) {
                    val categoryOptions = listOf("All") + taskList.map { it.category }.distinct().filter { it.isNotBlank() }
                    AlertDialog(
                        onDismissRequest = { showFilterDialog = false },
                        title = { Text("Filter Tasks") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Priority")
                                DropdownSelector(
                                    selected = selectedPriority,
                                    options = listOf("All", "High", "Medium", "Low"),
                                    onSelected = { selectedPriority = it }
                                )

                                Text("Category")
                                if (categoryOptions.size <= 1) {
                                    Text("No categories available", color = Color.Gray)
                                } else {
                                    DropdownSelector(
                                        selected = selectedCategory,
                                        options = categoryOptions,
                                        onSelected = { selectedCategory = it }
                                    )
                                }

                                Text("Due Date")
                                DropdownSelector(
                                    selected = selectedDue,
                                    options = listOf("All", "Today", "This Week", "Overdue"),
                                    onSelected = { selectedDue = it }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFilterDialog = false }) {
                                Text("Apply")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                selectedPriority = "All"
                                selectedCategory = "All"
                                selectedDue = "All"
                                showFilterDialog = false
                            }) {
                                Text("Reset")
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tasks match your filters.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            items(filteredTasks) { task ->
                                TaskItem(task = task) {
                                    val taskDetailsFragment = TaskDetailsFragment()
                                    val bundle = Bundle().apply { putInt("TASK_ID", task.id) }
                                    taskDetailsFragment.arguments = bundle
                                    val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(containerId, taskDetailsFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(containerId, AddTaskFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Add Task")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .width(50.dp)
                            .height(40.dp)
                            .background(
                                color = Color(0xFFF0F0F0),
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DropdownSelector(selected: String, options: List<String>, onSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onSelected(option)
                        expanded = false
                    }) {
                        Text(option)
                    }
                }
            }
        }
    }

    @Composable
    fun QuickSuggestionBanner(onClick: () -> Unit) {
        val tips = listOf(
            "Try reviewing your tasks at 6PM every day.",
            "Start your day with a high-priority task.",
            "Take short breaks to boost productivity.",
            "Log your water intake regularly.",
            "Spend 5 minutes planning tomorrow’s goals.",
            "Organize your workspace before starting."
        )
        val randomTip = remember { tips.random() }

        Card(
            backgroundColor = Color(0xFFEEF7FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("\uD83D\uDCA1 Smart AI Suggestion", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                Text(randomTip, style = MaterialTheme.typography.body2)
            }
        }
    }

    @Composable
    fun TaskItem(task: Task, onClick: (Task) -> Unit) {
        val priorityColor = when (task.priority) {
            "High" -> Color.Red
            "Medium" -> Color(0xFFFFA500)
            "Low" -> Color.Green
            else -> Color.Gray
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick(task) },
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(task.taskName, style = MaterialTheme.typography.h6)
                Text(task.description, style = MaterialTheme.typography.body2, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Priority: ${task.priority}", fontWeight = FontWeight.Bold, color = priorityColor)
                    Text("Category: ${task.category}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Due: ${task.dueDate}", fontWeight = FontWeight.Bold)
                    Text("Reminder: ${task.reminderTime}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    private fun getTodayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun isDueThisWeek(dueDate: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val due = formatter.parse(dueDate) ?: return false
            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val endOfWeek = calendar.time
            due.after(today) && due.before(endOfWeek)
        } catch (e: Exception) {
            false
        }
    }

    private fun isOverdue(dueDate: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val due = formatter.parse(dueDate)
            due != null && due.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}
