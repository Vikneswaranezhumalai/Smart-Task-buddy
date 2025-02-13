package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Locale


class AddTaskFragment : Fragment() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var voiceRecognitionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Properly initialize voiceRecognitionLauncher before fragment reaches RESUMED state
        voiceRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                spokenText?.let { processVoiceInput(it) }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)


        return ComposeView(requireContext()).apply {
            setContent {
                Column {
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
            }
        }
    }
    // ✅ Function to Start Voice Recognition
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        voiceRecognitionLauncher.launch(intent) // Use the properly initialized launcher
    }

    // ✅ Function to Process the Spoken Task Input
    private fun processVoiceInput(input: String) {
        println("Voice Input Received: $input")
        // Here you can process the text with NLP and update the UI
    }
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun AddTaskFragmentContent() {
        var naturalInput by remember { mutableStateOf("") }
        var taskName by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("Medium") }// Use string for dropdown
        var category by remember { mutableStateOf("General") } // Default category
        var dueDate by remember { mutableStateOf("") }
        var reminderTime by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }  // Dropdown expanded state
        var categoryExpanded by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }


        val priorities = listOf("High", "Medium", "Low")
        val categories = listOf("General", "Work & Productivity", "Personal & Home", "Health & Wellness", "Finance & Planning", "Social & Leisure")

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // 🔹 Voice Input + NLP Task Entry (Better UI)
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
                        // 🎤 Voice Input Button
                        Button(
                            onClick = { startVoiceRecognition() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Voice")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // ✅ NLP Processing Button
                        Button(
                            onClick = {
                                isProcessing = true
                                // Extract data from NLP
//                            val extractedData = processNaturalInput(naturalInput)
//                            taskName = extractedData.first
//                            dueDate = extractedData.second
//                            reminderTime = extractedData.third
                                isProcessing = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = naturalInput.isNotEmpty()
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Process Task")
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))
                // Task Name Input
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Priority Dropdown Menu
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
                            .clickable { expanded = !expanded }, // Toggle dropdown when clicked
                        readOnly = true,  // Make it read-only for selection
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        }
                    )

                    // Dropdown Menu for Priority
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

                // Category Dropdown
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

                // Due Date Input
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Reminder Time Input
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Reminder Time (hh:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add Task Button
                Button(
                    onClick = {
                        // Handle the task creation logic here
                        val newTask = Task(
                            taskName = taskName,
                            description = description,
                            priority = priority,  // String priority value
                            dueDate = dueDate,
                            category = category, // Added category field
                            reminderTime = reminderTime
                        )
                        taskViewModel.addTask(requireContext(), newTask)
                        println("Task Created: $taskName")
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
