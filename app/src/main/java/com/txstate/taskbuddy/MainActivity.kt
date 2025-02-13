package com.txstate.taskbuddy

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.txstate.taskbuddy.fragments.TaskListFragment
import com.txstate.taskbuddy.ui.theme.TaskBuddyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskBuddyTheme  {
                    FragmentHost(fragmentManager = supportFragmentManager)
            }
        }
    }
}

@Composable
fun FragmentHost(fragmentManager: FragmentManager) {
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply {
                id = View.generateViewId() // Generate a unique ID for the container
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { containerView ->
        val fragmentManager = (containerView.context as AppCompatActivity).supportFragmentManager
        // Initialize the first fragment
        if (fragmentManager.findFragmentById(containerView.id) == null) {
            fragmentManager.beginTransaction()
                .add(containerView.id, TaskListFragment()) // Start with FragmentOne
                .commit()
        }
    }
}