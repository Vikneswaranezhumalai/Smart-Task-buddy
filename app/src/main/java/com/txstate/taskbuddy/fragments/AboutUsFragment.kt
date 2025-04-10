package com.txstate.taskbuddy.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar

class AboutUsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setContent {
                Column(modifier = Modifier.fillMaxSize()) {
                    CommonToolbar(
                        title = "About Us",
                        navigationIcon = Icons.Default.ArrowBack,
                        onBackButtonClick = {
                            requireActivity().supportFragmentManager.popBackStack()
                        },
                        onCompletedTasksClick = null
                    )

                    // About content
                    AboutUsScreen()

                    // Spacer for visual separation
                    Spacer(modifier = Modifier.height(16.dp))

                    // New Contact Us section
                    ContactUsCard()
                }
            }
        }
    }
}


@Composable
fun AboutUsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "About This Project",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "TaskBuddy is a student project designed to help users manage daily tasks, " +
                            "get smart AI-powered recommendations, and stay productive.",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Developed by students at Texas State University as part of coursework.",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/*
 * Code modified by Meghashree Kulkarni Raghavendra.
 * a "Contact Us" card—that lets users tap an email address to open their email client
 */
@Composable
fun ContactUsCard() {
    val context = LocalContext.current
    Card(
        elevation = 8.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contact Us",
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "Email: zuz11@txstate.edu",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    // Create an email intent and launch the email client
                    val emailIntent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:info@txstate.edu")
                    )
                    context.startActivity(emailIntent)
                }
            )
        }
    }
}
