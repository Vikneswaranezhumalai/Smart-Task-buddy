package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AddTaskFragment : Fragment() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var voiceRecognitionLauncher: ActivityResultLauncher<Intent>
    private val naturalInputState = mutableStateOf("")
    private val isProcessingState = mutableStateOf(false)
    val priorities = listOf("High", "Medium", "Low")
    val categories = listOf("General", "Work & Productivity", "Personal & Home", "Health & Wellness", "Finance & Planning", "Social & Leisure")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voiceRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
                println("🔹 Voice Recognition Result: $spokenText")
                naturalInputState.value = spokenText
                processVoiceInput(spokenText)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setContent {
                val isProcessing by isProcessingState
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CommonToolbar(
                            title = "Add Task",
                            navigationIcon = Icons.Default.ArrowBack,
                            onBackButtonClick = {
                                requireActivity().supportFragmentManager.popBackStack()
                            },
                            onCompletedTasksClick = null
                        )
                        AddTaskFragmentContent()
                    }
                    println("Loading state chek: " + isProcessingState.value)
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
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        voiceRecognitionLauncher.launch(intent)
    }

    private fun processVoiceInput(input: String) {
        println("Voice Input Received: $input")
        isProcessingState.value = true
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                taskViewModel.processNaturalInput(input,
                    onSuccess = { newTask ->
                        taskViewModel.addTask(requireContext(), newTask)
                        Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to process task", Toast.LENGTH_SHORT).show()
            } finally {
            }
        }
    }

    private fun processTaskInput(
        input: String,
        coroutineScope: CoroutineScope,
        context: Context,
        taskViewModel: TaskViewModel,
        onComplete: (Task) -> Unit
    ) {
        isProcessingState.value = true
        coroutineScope.launch {

            try {
                taskViewModel.processNaturalInput(input,
                    onSuccess = { newTask ->
                        taskViewModel.addTask(context, newTask)
                        Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                        onComplete(newTask)
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to process task", Toast.LENGTH_SHORT).show()
            } finally {
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun AddTaskFragmentContent() {

        var naturalInput by naturalInputState
        val isProcessing = isProcessingState.value
        var taskName by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("Medium") }
        var category by remember { mutableStateOf("General") }
        var dueDate by remember { mutableStateOf("") }
        var reminderTime by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text("Task Input", style = MaterialTheme.typography.h6)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(0.5.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    OutlinedTextField(
                        value = naturalInput,
                        onValueChange = { naturalInput = it },
                        label = { Text("Enter Task (or Use Voice)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isProcessingState.value = true
                                startVoiceRecognition() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Voice")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                processTaskInput(
                                    input = naturalInput,
                                    coroutineScope = coroutineScope,
                                    context = context,
                                    taskViewModel = taskViewModel,
                                    onComplete = { newTask ->
                                        taskName = newTask.taskName
                                        dueDate = newTask.dueDate
                                        reminderTime = newTask.reminderTime
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = naturalInput.isNotEmpty()
                        ) {
                                Text("Process Task")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        label = { Text("Priority") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        priorities.forEach { priorityOption ->
                            DropdownMenuItem(onClick = {
                                priority = priorityOption
                                expanded = false
                            }) {
                                Text(text = priorityOption)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = !categoryExpanded },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { categoryExpanded = !categoryExpanded }
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(onClick = {
                                category = option
                                categoryExpanded = false
                            }) {
                                Text(text = option)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Reminder Time (hh:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                val loggedInUser = taskViewModel.getLoggedInUser()

                Button(
                    onClick = {
                        val newTask = Task(
                            taskName = taskName,
                            description = description,
                            priority = priority,
                            dueDate = dueDate,
                            category = category,
                            reminderTime = reminderTime,
                            userId = loggedInUser?.id ?: 0
                        )
                        taskViewModel.addTask(requireContext(), newTask)
                        Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = taskName.isNotEmpty() && description.isNotEmpty() && dueDate.isNotEmpty() && reminderTime.isNotEmpty()
                ) {
                    Text("Add Task")
                }
            }
        }
    }
}
