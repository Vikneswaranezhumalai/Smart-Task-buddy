package com.txstate.taskbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.txstate.taskbuddy.ui.theme.components.CommonToolbar

/**
 * ContactUsV2.kt
 * Created by Arpita Chowdhury
 *
 * A refined Contact Us screen layout with updated typography,
 * clearer structure, and additional support details for a better
 * user support experience.
 */

class ContactUsV2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setContent {
                Column(modifier = Modifier.fillMaxSize()) {
                    CommonToolbar(
                        title = "Contact Us",
                        navigationIcon = Icons.Default.ArrowBack,
                        onBackButtonClick = {
                            requireActivity().supportFragmentManager.popBackStack()
                        },
                        onCompletedTasksClick = null
                    )

                    ContactUsScreenV2()
                }
            }
        }
    }
}

@Composable
fun ContactUsScreenV2() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 10.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Get in Touch",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "We're here to help! Reach out with any questions or feedback.",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "📧 vyv15@txstate.edu",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
                )

                Text(
                    text = "📞 (512) 245-0000",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
                )

                Text(
                    text = "🕒 Support: 9 AM – 5 PM CST (Mon–Fri)",
                    style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.primaryVariant)
                )
            }
        }
    }
}
