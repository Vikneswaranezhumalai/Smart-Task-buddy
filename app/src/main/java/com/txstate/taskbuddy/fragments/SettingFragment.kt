package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*

import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.MainActivity
import com.txstate.taskbuddy.TaskViewModel
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository


class SettingFragment : Fragment(){
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        val userRepository =
            UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
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
    fun SettingFragmentContent() {
        Scaffold(
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
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
                    // Handle Contact Us click
                }
                Divider()
                SettingOption(icon = Icons.Default.Info, title = "About Us") {
                    // Handle About Us click
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
            modifier = Modifier
                .clickable(onClick = onClick),
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

