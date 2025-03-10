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
import androidx.lifecycle.ViewModelProvider
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.fragments.LoginFragment
import com.txstate.taskbuddy.fragments.TaskListFragment
import com.txstate.taskbuddy.ui.theme.TaskBuddyTheme

class MainActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var userViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userRepository = UserRepository(TaskDatabase.getDatabase(this).userDao())
            userViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

            authManager = AuthManager(this, userRepository)
            var activtyStack = "DEFALUT";
            // Check if the user is logged in
            authManager.saveLoginState(false)
            if (authManager.isLoggedIn()) {
                // Navigate to task list
                activtyStack = "LIST_FRAGMENT"
                //activtyStack = "LOGIN_FRAGMENT"
            } else {
                activtyStack = "LOGIN_FRAGMENT"
            }
            userViewModel.insertDummyUser()
            TaskBuddyTheme  {
                    FragmentHost(fragmentManager = supportFragmentManager,  activtyStack)
            }
        }
    }

}

@Composable
fun FragmentHost(fragmentManager: FragmentManager, activtyStack: String) {
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
            when (activtyStack) {
                "DEFALUT" ,
                "LIST_FRAGMENT" ->
                    {
                    fragmentManager.beginTransaction()
                        .add(containerView.id, TaskListFragment()) // Start with FragmentOne
                        .commit()
                }
                "LOGIN_FRAGMENT" -> {
                    fragmentManager.beginTransaction()
                        .add(containerView.id, LoginFragment()) // Add LoginFragment
                        .commit()
                }
            }
    }
}
    }