package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.MainActivity
import com.txstate.taskbuddy.R
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar

class SettingFragment : Fragment() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var authManager: AuthManager
    val allBadges = listOf("10 Tasks", "20 Tasks", "50 Tasks", "100 Tasks")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        val userRepository = UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
        authManager = AuthManager(requireContext(), userRepository)

        return ComposeView(requireContext()).apply {
            setContent {
                Column {
                    CommonToolbar(
                        title = "Task Settings",
                        navigationIcon = Icons.Default.ArrowBack,
                        onBackButtonClick = {
                            requireActivity().supportFragmentManager.popBackStack()
                        },
                        onCompletedTasksClick = null
                    )
                    SettingFragmentContent()
                }
            }
        }
    }
    @Composable
    fun BadgeGallery(unlockedBadges: List<String>) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🏆 Badge Gallery", style = MaterialTheme.typography.h6)

            Spacer(modifier = Modifier.height(8.dp))

            allBadges.forEach { badge ->
                val isUnlocked = unlockedBadges.contains(badge)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = badge,
                        tint = if (isUnlocked) Color(0xFFFFD700) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = badge,
                            fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                            color = if (isUnlocked) Color.Black else Color.Gray
                        )
                        Text(
                            text = if (isUnlocked) "Unlocked!" else "Locked",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun SettingFragmentContent() {
        val user = authManager.getLoggedInUser()
        val unlockedBadges by taskViewModel.unlockedBadges.collectAsState()


        // Trigger a recheck of the badge when this screen opens
        LaunchedEffect(true) {
            user?.let {
                taskViewModel.refreshBadgeStatus(it.id)
            }
        }

        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile & Achievements
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "\uD83D\uDC64 Profile & Achievements",
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Name: ${user?.name ?: "N/A"}", style = MaterialTheme.typography.body1)
                    Text("Email: ${user?.email ?: "N/A"}", style = MaterialTheme.typography.body1)

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("🏅 Achievements", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (unlockedBadges.isNotEmpty()) {
                        unlockedBadges.forEach { badge ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = badge,
                                    tint = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(badge, fontWeight = FontWeight.Bold)
                                    Text("Unlocked for completing $badge.")
                                }
                            }
                        }
                    } else {
                        Text("No achievements unlocked yet.", color = Color.Gray)
                    }

                    BadgeGallery(unlockedBadges = unlockedBadges)
                }

                Divider()
                SettingOption(icon = Icons.Default.History, title = "Completed Task History") {
                    val taskHistoryFragment = TaskHistoryFragment()
                    val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(containerId, taskHistoryFragment)
                        .addToBackStack(null)
                        .commit()
                }

                Divider()
                SettingOption(icon = Icons.Default.Email, title = "Contact Us") {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ContactUs())
                        .addToBackStack(null)
                        .commit()
                }

                Divider()
                SettingOption(icon = Icons.Default.Info, title = "About Us") {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutUsFragment())
                        .addToBackStack(null)
                        .commit()
                }

                Divider()
                SettingOption(icon = Icons.Default.Logout, title = "Logout") {
                    (activity as? MainActivity)?.logoutAndClearToLogin()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun SettingOption(icon: ImageVector, title: String, onClick: () -> Unit) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = title
                )
            },
            text = {
                Text(text = title)
            }
        )
    }
}
