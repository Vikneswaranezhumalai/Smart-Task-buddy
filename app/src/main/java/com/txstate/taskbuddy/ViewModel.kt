package com.txstate.taskbuddy


import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.txstate.taskbuddy.apiCall.ApiConstants
import com.txstate.taskbuddy.apiCall.Message
import com.txstate.taskbuddy.apiCall.OpenAIRequest
import com.txstate.taskbuddy.apiCall.RetrofitInstance
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.CategorySummary
import com.txstate.taskbuddy.database.ExtractedTask
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.TaskRepository
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.database.Users
import com.txstate.taskbuddy.notifications.TaskNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    // Room Database Instance
    private val taskRepository: TaskRepository
    private val userRepository: UserRepository
    val getAllTasks: LiveData<List<Task>>
    val getCompletedTasks: LiveData<List<Task>>
    private lateinit var authManager: AuthManager
    private var userId = -1;
    // Publicly readable, internally mutable
    private val _badgeUnlocked = MutableStateFlow(false)
    val badgeUnlocked: StateFlow<Boolean> = _badgeUnlocked
    val _unlockedBadges = MutableStateFlow<List<String>>(emptyList())
    val unlockedBadges: StateFlow<List<String>> = _unlockedBadges



    init {
        val taskDatabase = TaskDatabase.getDatabase(application)
        taskRepository = TaskRepository(taskDatabase.taskDao())
        userRepository = UserRepository(taskDatabase.userDao())
        authManager = AuthManager(application, userRepository);
        val loggedInUser = authManager.getLoggedInUser()
        if (loggedInUser != null) {
            userId = loggedInUser.id
            Log.d("UserViewModel", "userId: $userId")
            getAllTasks = taskRepository.getAllTasks(userId)
            Log.d("UserViewModel", "getAllTasks: $getAllTasks")
            getCompletedTasks = taskRepository.getCompletedTask(userId)
        } else {
            Log.d("UserViewModel", "EMPTY:")
            // Initialize with empty LiveData or default values
            getAllTasks = MutableLiveData(emptyList())
            getCompletedTasks = MutableLiveData(emptyList())

            Log.d("UserViewModel", "No user logged in")
        }

    }
    fun deleteAllUsers() {
        GlobalScope.launch {
            userRepository.deleteAll()
        }
    }
    fun getLoggedInUser(): Users? {
        return authManager.getLoggedInUser()
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
    fun refreshBadgeStatus(userId: Int) {
        GlobalScope.launch {
            val count = taskRepository.getCompletedTaskCount(userId)
            val badges = mutableListOf<String>()

            if (count >= 10) badges.add("10 Tasks")
            if (count >= 20) badges.add("20 Tasks")
            if (count >= 50) badges.add("50 Tasks")
            if (count >= 100) badges.add("100 Tasks")

            _unlockedBadges.value = badges
        }
    }


    // LiveData for a single task
    fun getTaskById(taskId: Int): LiveData<Task> {
        return taskRepository.getTaskById(taskId)
    }

    fun updateTaskStatus(task: Task, context: Context) {
        GlobalScope.launch {
            taskRepository.updateTask(task)
            val previousCount = taskRepository.getCompletedTaskCount(task.userId) - 1
            val currentCount = taskRepository.getCompletedTaskCount(task.userId)

            val newBadge = when {
                previousCount < 10 && currentCount >= 10 -> "10 Tasks"
                previousCount < 20 && currentCount >= 20 -> "20 Tasks"
                previousCount < 50 && currentCount >= 50 -> "50 Tasks"
                previousCount < 100 && currentCount >= 100 -> "100 Tasks"
                else -> null
            }
            if (newBadge != null) {
                _badgeUnlocked.value = true
                TaskNotificationWorker.sendBadgeUnlockedNotification(context, newBadge)
            }
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
    fun fetchFrequentCategories(onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val lastMonthDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)

            val topCategories = withContext(Dispatchers.IO) {
                taskRepository.getFrequentCategories(lastMonthDate)
            }.map { it.category }

            onResult(topCategories)
        }
    }


    // ✅ Function to Process Task using OpenAI
    fun processNaturalInput(input: String, onSuccess: (Task) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                println("🔹 Processing Task Input: $input")

                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                // ✅ OpenAI Request with JSON Response
                val request = OpenAIRequest(
                    model = ApiConstants.OpenAIModels.GPT_4_TURBO,
                    messages = listOf(
                        Message("system", """
                            You are an AI assistant that extracts structured task details from natural language processing.
                            ### Instructions:
                            - Extract details for **Task Name, Description, Category, Priority, Due Date, and Reminder Time**.
                            - **Task Name** should be short (max 5 words).
                            - **Reminder Time** should be a short, clear summary (max 5 words).
                            - **Priority Levels:**
                              - **High** → Urgent, deadlines, work-related, critical tasks.
                              - **Medium** → Regular daily tasks, important but not urgent.
                              - **Low** → Optional or flexible tasks, leisure, future plans.
                              - **If no priority is mentioned, default to `Medium`.**
                              - **For Work & Productivity or Health & Wellness categories, default priority to `High`.**
                            - **Categories:**
                              - **General** → Default for uncategorized tasks.
                              - **Work & Productivity** → Office tasks, reports, meetings, projects.
                              - **Personal & Home** → Household chores, shopping, errands.
                              - **Health & Wellness** → Doctor appointments, fitness, self-care, medicine.
                              - **Finance & Planning** → Budgeting, bills, taxes, financial planning.
                              - **Social & Leisure** → Hangouts, movies, vacations, personal fun.
                              - **If no category is mentioned, default to `General`.**
                            - **ALWAYS include a Due Date in YYYY-MM-DD format.** If the user does not mention a date, set it to today's date (${LocalDate.now()}).
                            - Convert relative dates:
                              - **"tomorrow" → YYYY-MM-DD**
                              - **"next Monday" → YYYY-MM-DD**
                              - **"in 3 days" → YYYY-MM-DD**
                            - **NEVER return 'tomorrow' or 'next Monday' in the response.** Always replace it with an absolute date.
                            - **Today's date is $todayDate**. Use this to calculate all relative dates.
                            - **Reminder Time**
                              - Convert times into **HH:mm format** (24-hour).
                              - If the user mentions a time (e.g., "morning," "evening," "at 9 AM"), extract only the time.
                              - ✅ **If no reminder time is mentioned, set it to `09:00` (morning reminder).**
                            - Only return valid JSON. No extra text.
                        """.trimIndent()),

                        Message("user", """
                            Extract task details from: "$input"
                            Convert relative dates to absolute **YYYY-MM-DD** format based on today's date.
                            Return JSON in this exact format (inside a code block):
                            ```json
                            {
                                "taskName": "<task_name>",
                                "description": "<task_description>",
                                "category": "<category>",
                                "priority": "<priority>",
                                "dueDate": "<yyyy-MM-dd>",  
                                "reminderTime": "<HH:mm>"
                            }
                            ```
                        """.trimIndent())
                    ),
                    max_tokens = 100,
                    temperature = 0.3
                )

                println("🔹 JSON Payload Sent: ${Json.encodeToString(request)}")

                // ✅ Call OpenAI API
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getCompletion(request)
                }

                println("🔹 OpenAI API Response: $response")

                val extractedText = response.choices.firstOrNull()?.message?.content ?: "{}"
                val cleanedJson = extractJsonContent(extractedText)

                val extractedTask = try {
                    Json { ignoreUnknownKeys = true }.decodeFromString<ExtractedTask>(cleanedJson)
                } catch (e: Exception) {
                    println(" JSON Parsing Error: ${e.message}")
                    ExtractedTask("Untitled Task", "No description", "General", "Medium", "2025-12-31", "12:00")
                }

                val dueDate = validateAndFixDate(extractedTask.dueDate)
                val loggedInUser = authManager.getLoggedInUser();
                if (loggedInUser != null) {
                    val finalTask = Task(
                        taskName = extractedTask.taskName.ifBlank { "Untitled Task" },
                        description = extractedTask.description.ifBlank { "No description" },
                        category = extractedTask.category.ifBlank { "General" },
                        priority = extractedTask.priority.ifBlank { "Medium" },
                        dueDate = dueDate,
                        reminderTime = extractedTask.reminderTime.ifBlank { "12:00" },
                        userId = loggedInUser.id
                    )
                    onSuccess(finalTask)
                } else {
                    // Clearly handle scenario if no user is logged in.
                    onError( "No user logged in. Please log in again.")
                }

            } catch (e: Exception) {
                println(" OpenAI API Error: ${e.message}")
                onError("Failed to process task")
            }
        }
    }
    private val _recommendedTasks = MutableStateFlow<List<Task>>(emptyList())
    val recommendedTasks: StateFlow<List<Task>> = _recommendedTasks

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadRecommendations() {
        recommendTasksIncludingAll(
            onSuccess = { tasks -> _recommendedTasks.value = tasks },
            onError = { error -> _errorMessage.value = error }
        )
    }

    fun recommendTasksIncludingAll(
        onSuccess: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                println("🔹 Fetching All Tasks and Recommendations...")

                val lastMonthDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)

                // ✅ Step 1: Get all tasks from DB
                val allTasks = withContext(Dispatchers.IO) {
                    taskRepository.getCompletedTasks(userId) // make sure this DAO method exists
                }

                // ✅ Step 2: Get frequently used categories

                val frequentCategories = withContext(Dispatchers.IO) {
                    taskRepository.getFrequentCategories(lastMonthDate)
                    }

                // ✅ Step 3: Generate AI recommendations
                val aiGeneratedTasks = fetchAITaskRecommendations(frequentCategories)

                // ✅ Step 4: Combine and return
                val combinedTasks = (allTasks + aiGeneratedTasks)
                    .distinctBy { it.taskName }
                    .shuffled()

                if (combinedTasks.isNotEmpty()) {
                    onSuccess(combinedTasks)
                } else {
                    onError("No tasks or recommendations found.")
                }

            } catch (e: Exception) {
                println("❌ Error combining tasks: ${e.message}")
                onError("Something went wrong.")
            }
        }
    }


    private suspend fun fetchAITaskRecommendations(categories: List<CategorySummary>): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                println("\uD83D\uDD39 Fetching AI Tasks from OpenAI GPT API...")

                val request = OpenAIRequest(
                    model = ApiConstants.OpenAIModels.GPT_4_TURBO,
                    messages = listOf(
                        Message("system", """
                        You are a task recommendation AI. 
                        Generate 3 personalized tasks based on these categories: ${categories.joinToString()}.
                        Each task must include:
                        - Task Name (max 5 words)
                        - Short Description
                        - Category (General, Work & Productivity, Personal & Home, Health & Wellness, Finance & Planning, Social & Leisure)
                        - Priority (High, Medium, Low)
                        - Due Date (YYYY-MM-DD)
                        - Reminder Time (HH:mm)
                        Return only valid JSON, no markdown or explanation. Do not include trailing commas.
                    """.trimIndent()),
                        Message("user", """
                        Format:
                        [
                            {
                                "taskName": "Morning Walk",
                                "description": "Walk for 20 minutes",
                                "category": "Health & Wellness",
                                "priority": "Medium",
                                "dueDate": "2025-04-03",
                                "reminderTime": "07:00"
                            }
                        ]
                    """.trimIndent())
                    ),
                    max_tokens = 1000,
                    temperature = 0.4
                )

                val response = RetrofitInstance.api.getCompletion(request)
                val content = response.choices.firstOrNull()?.message?.content

                if (content.isNullOrBlank()) {
                    println("\u26A0\uFE0F OpenAI returned empty content.")
                    return@withContext emptyList()
                }

                println("\uD83D\uDD39 Raw GPT Response:\n$content")

                // Step 1: Clean GPT JSON (remove markdown/code block/trailing commas)
                val cleanedJson = extractJsonContentImproved(content)

                println("\uD83D\uDD39 Extracted JSON:\n$cleanedJson")

                // Step 2: Parse JSON to List<ExtractedTask>
                val extractedTasks = try {
                    Json.decodeFromString<List<ExtractedTask>>(cleanedJson)
                } catch (e: Exception) {
                    println("\u26A0\uFE0F Not an array. Trying single object: ${e.message}")
                    try {
                        listOf(Json.decodeFromString<ExtractedTask>(cleanedJson))
                    } catch (e2: Exception) {
                        println("\u274C Still failed parsing: ${e2.message}")
                        return@withContext emptyList()
                    }
                }

                return@withContext extractedTasks.map {
                    println("\u2705 Parsed Task: ${Json.encodeToString(it)}")
                    Task(
                        userId = 0, // TODO: Replace with actual user ID
                        taskName = it.taskName,
                        description = it.description,
                        category = it.category,
                        priority = it.priority,
                        dueDate = it.dueDate,
                        reminderTime = it.reminderTime
                    )
                }

            } catch (e: Exception) {
                println("\u274C AI Task Fetching Error: ${e.message}")
                emptyList()
            }
        }
    }


    fun extractJsonContent(responseText: String): String {
        val jsonStart = responseText.indexOf("{")  // First '{' in response
        val jsonEnd = responseText.lastIndexOf("}") // Last '}'

        return if (jsonStart != -1 && jsonEnd != -1) {
            responseText.substring(jsonStart, jsonEnd + 1).trim()  // Extract JSON block
        } else {
            "{}"  // Fallback to empty JSON if parsing fails
        }
    }
    fun extractJsonContentImproved(content: String): String {
        return content
            .substringAfter("```json", "")
            .substringBefore("```", "")
            .ifBlank { content }
            .trim()
            .replace(Regex(""",\s*\}"""), "}") // Remove trailing comma before }
            .replace(Regex(""",\s*\]"""), "]") // Remove trailing comma before ]
    }
    private fun validateAndFixDate(dateString: String): String {
        val today = LocalDate.now()

        return when (dateString.lowercase(Locale.ROOT)) {
            "today" -> today.toString()
            "tomorrow" -> today.plusDays(1).toString()
            "day after tomorrow" -> today.plusDays(2).toString()
            "next monday" -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toString()
            "next tuesday" -> today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).toString()
            "next wednesday" -> today.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).toString()
            "next thursday" -> today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).toString()
            "next friday" -> today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).toString()
            "next saturday" -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString()
            "next sunday" -> today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).toString()
            else -> {
                try {
                    // ✅ If already in valid format, return as is
                    LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE).toString()
                } catch (e: Exception) {
                    println("⚠️ Invalid date received: $dateString, using today's date instead.")
                    today.toString()
                }
            }
        }
    }
}
