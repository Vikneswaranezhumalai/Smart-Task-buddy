import androidx.fragment.app.testing.FragmentScenario
import com.txstate.taskbuddy.fragments.*
import org.junit.Assert.*
import org.junit.Test

class TaskListFragmentTest {
    @Test
    fun `launches TaskListFragment successfully`() {
        val scenario = FragmentScenario.launchInContainer(TaskListFragment::class.java)
        assertNotNull(scenario)
    }
}

class TaskDetailsFragmentTest {
    @Test
    fun `launches TaskDetailsFragment with argument`() {
        val bundle = android.os.Bundle().apply { putInt("TASK_ID", 1) }
        val scenario = FragmentScenario.launchInContainer(TaskDetailsFragment::class.java, bundle)
        assertNotNull(scenario)
    }
}

class RecommendationFragmentTest {
    @Test
    fun `launches RecommendationFragment`() {
        val scenario = FragmentScenario.launchInContainer(RecommendationFragment::class.java)
        assertNotNull(scenario)
    }
}

class TaskHistoryFragmentTest {
    @Test
    fun `launches TaskHistoryFragment`() {
        val scenario = FragmentScenario.launchInContainer(TaskHistoryFragment::class.java)
        assertNotNull(scenario)
    }
}

class SettingFragmentTest {
    @Test
    fun `launches SettingFragment`() {
        val scenario = FragmentScenario.launchInContainer(SettingFragment::class.java)
        assertNotNull(scenario)
    }
}
