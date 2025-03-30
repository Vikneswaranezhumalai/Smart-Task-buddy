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
import com.txstate.taskbuddy.database.ExtractedTask
import com.txstate.taskbuddy.database.Task
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.TaskRepository
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.database.Users
import com.txstate.taskbuddy.notifications.TaskNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

//    fun recommendTasks(onSuccess: (List<Task>) -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            try {
//                println("🔹 Fetching Recommended Tasks...")
//
//                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
//                val lastMonthDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)
//
//                // ✅ Step 1: Fetch High-Priority & Frequently Done Tasks
//                val highPriorityTasks = withContext(Dispatchers.IO) {
//                    //taskRepository.getHighPriorityTasks(todayDate)
//
//                }
//
//                val frequentCategories = withContext(Dispatchers.IO) {
//                    taskDao.getFrequentCategories(lastMonthDate)
//                }.map { it.category }
//
//                val recentCompletedTasks = withContext(Dispatchers.IO) {
//                    taskDao.getRecentCompletedTasks()
//                }
//
//                // ✅ Step 2: Fetch AI-Generated Task Recommendations (10 Tasks)
//                val aiGeneratedTasks = fetchAITaskRecommendations(frequentCategories)
//
//                // ✅ Step 3: Merge All Recommendations
//                val recommendedTasks = (highPriorityTasks + aiGeneratedTasks).distinct().take(10)
//
//                if (recommendedTasks.isNotEmpty()) {
//                    onSuccess(recommendedTasks)
//                } else {
//                    onError("No recommended tasks available.")
//                }
//
//            } catch (e: Exception) {
//                println("❌ Task Recommendation Error: ${e.message}")
//                onError("Failed to fetch recommendations")
//            }
//        }
//    }

    // ✅ Fetch 10 Task Recommendations from OpenAI
    private suspend fun fetchAITaskRecommendations(categories: List<String>): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val request = OpenAIRequest(
                    model = ApiConstants.OpenAIModels.GPT_4_TURBO,
                    messages = listOf(
                        Message("system", """
                            You are a task recommendation AI. 
                            Generate **10 personalized tasks** based on these categories: ${categories.joinToString()}.
                            **Each task should include**:
                            - **Task Name** (Max 5 words)
                            - **Short Description**
                            - **Category** (One of: General, Work & Productivity, Personal & Home, Health & Wellness, Finance & Planning, Social & Leisure)
                            - **Priority** (High, Medium, Low)
                            - **Due Date** (Format: YYYY-MM-DD)
                            - **Reminder Time** (Format: HH:mm)
                            - Only return valid JSON.
                        """.trimIndent()),

                        Message("user", """
                            Generate 10 tasks in JSON format:
                            ```json
                            [
                                {
                                    "taskName": "<task_name>",
                                    "description": "<task_description>",
                                    "category": "<category>",
                                    "priority": "<priority>",
                                    "dueDate": "<yyyy-MM-dd>",  
                                    "reminderTime": "<HH:mm>"
                                }
                            ]
                            ```
                        """.trimIndent())
                    ),
                    max_tokens = 200,
                    temperature = 0.4
                )

                println("🔹 Fetching 10 AI Tasks from OpenAI GPT API...")

                val response = RetrofitInstance.api.getCompletion(request)

                println("🔹 AI Response: $response")

                val extractedJson = extractJsonContent(response.choices.firstOrNull()?.message?.content ?: "[]")

                Json { ignoreUnknownKeys = true }.decodeFromString<List<Task>>(extractedJson)

            } catch (e: Exception) {
                println("❌ AI Task Fetching Error: ${e.message}")
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
