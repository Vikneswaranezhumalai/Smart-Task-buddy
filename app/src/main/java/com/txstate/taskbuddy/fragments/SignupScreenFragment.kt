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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val userRepository =
                    UserRepository(TaskDatabase.getDatabase(requireContext()).userDao())
                authManager = AuthManager(requireContext(), userRepository)

                SignUpScreen(authManager, requireContext())
            }
        }
    }

    @Composable
    fun SignUpScreen(authManager: AuthManager, context: Context) {
        var email by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_register_1),
                    contentDescription = "SignUp Icon",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 40.dp, bottom = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Create a new account",
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )

                Button(
                    onClick = {
                        if (password == confirmPassword && email.isNotEmpty()) {
                            isLoading = true
                            GlobalScope.launch(Dispatchers.IO) {
                                val success = authManager.register(email, password,name)
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                        requireActivity().supportFragmentManager.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading
                ) {
                    Text("Sign Up")
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

                TextButton(onClick = { requireActivity().supportFragmentManager.popBackStack() }) {
                    Text("Already have an account? Login")
                }
            }
        }
    }
    @Preview(showBackground = true)
    @Composable
    fun SignUpScreenPreview() {
        SignUpScreen(authManager = AuthManager(LocalContext.current, UserRepository(TaskDatabase.getDatabase(LocalContext.current).userDao())), context = LocalContext.current)
    }
}
