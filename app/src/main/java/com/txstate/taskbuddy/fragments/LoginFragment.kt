package com.txstate.taskbuddy.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.txstate.taskbuddy.R
import com.txstate.taskbuddy.database.AuthManager
import com.txstate.taskbuddy.database.TaskDatabase
import com.txstate.taskbuddy.database.UserRepository
import com.txstate.taskbuddy.ui.theme.TaskBuddyTheme
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                // Initialize AuthManager with UserRepository
                val userRepository =
                    UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
                authManager = AuthManager(requireContext(), userRepository)

                // Calling the Composable function for the LoginScreen
                LoginScreen(authManager, requireContext())
            }
        }
    }


    @Composable
    fun LoginScreen(authManager: AuthManager, context: Context) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        email = "vezhumal@gmail.com"
        password = "vezhumal"

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Image at the top
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_1), // Replace with actual image
                    contentDescription = "Login Icon",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 40.dp, bottom = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )

                // Title
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Please sign in to continue",
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Email Input Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { /* Handle next */ })
                )

                // Password Input Field
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { /* Handle done */ })
                )

                // Login Button
                Button(
                    onClick = {
                        isLoading = true
                        GlobalScope.launch(Dispatchers.IO) {
                            val success = authManager.login(email, password)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    val taskListFragment = TaskListFragment()
                                    val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(containerId, taskListFragment)
                                        .commit()
                                } else {
                                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
                ) {
                    Text("Login")
                }

                // Show loading indicator while logging in
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

                // "Don't have an account? Sign Up" button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Don't have an account?", style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        val SignUpFragment = SignUpFragment()
                        val containerId = (view?.parent as? ViewGroup)?.id ?: 0
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(containerId, SignUpFragment)
                            .addToBackStack(null)
                            .commit()
                    }) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewLoginScreen() {
        TaskBuddyTheme {
            val context = LocalContext.current
            val mockAuthManager = AuthManager(context, UserRepository(TaskDatabase.getDatabase(context).userDao()))
            LoginScreen(authManager = mockAuthManager, context = context)
        }
    }
}



