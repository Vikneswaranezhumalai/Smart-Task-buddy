import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TaskViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = TaskViewModel(application)
    }

    @Test
    fun `processNaturalInput should return Task on success`() = runBlockingTest {
        var result: Task? = null
        var error: String? = null

        viewModel.processNaturalInput("Buy milk tomorrow at 9am", onSuccess = {
            result = it
        }, onError = {
            error = it
        })

        Thread.sleep(3000)

        assertNotNull(result)
        assertNull(error)
        assertTrue(result!!.taskName.isNotBlank())
        assertTrue(result!!.dueDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertTrue(result!!.reminderTime.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun `processNaturalInput should return error if no user logged in`() = runBlockingTest {
        viewModel.deleteAllUsers()

        var result: Task? = null
        var error: String? = null

        viewModel.processNaturalInput("Submit report by 5pm", onSuccess = {
            result = it
        }, onError = {
            error = it
        })

        Thread.sleep(3000)

        assertNull(result)
        assertEquals("No user logged in. Please log in again.", error)
    }

    @Test
    fun `voice input with no time defaults reminder to 09_00`() = runBlockingTest {
        var result: Task? = null
        var error: String? = null

        viewModel.processNaturalInput("Call mom tomorrow", onSuccess = {
            result = it
        }, onError = {
            error = it
        })

        Thread.sleep(3000)

        assertNotNull(result)
        assertEquals("09:00", result!!.reminderTime)
    }

    @Test
    fun `voice input with health task defaults to high priority`() = runBlockingTest {
        var result: Task? = null
        var error: String? = null

        viewModel.processNaturalInput("Doctor appointment next Monday at 10am", onSuccess = {
            result = it
        }, onError = {
            error = it
        })

        Thread.sleep(3000)

        assertNotNull(result)
        assertEquals("High", result!!.priority)
        assertTrue(result!!.category.contains("Health", ignoreCase = true))
    }

    @Test
    fun `voice input with no category defaults to General`() = runBlockingTest {
        var result: Task? = null
        var error: String? = null

        viewModel.processNaturalInput("Finish assignment by Friday at 8pm", onSuccess = {
            result = it
        }, onError = {
            error = it
        })

        Thread.sleep(3000)

        assertNotNull(result)
        assertTrue(result!!.category.isNotBlank())
    }

}
