package com.txstate.taskbuddy.ui.theme.components


import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CommonToolbar(
    title: String,
    onBackButtonClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    onCompletedTasksClick: (() -> Unit)? = null , // Add optional click handler for the completed tasks icon
    onSettingsClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = if (onBackButtonClick != null && navigationIcon != null) {
            {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Back Button"
                    )
                }
            }
        } else null,
        actions = {
            // Show the Completed Tasks icon if onCompletedTasksClick is provided
            if (onCompletedTasksClick != null) {
                IconButton(onClick = onCompletedTasksClick) {
                    Icon(
                        imageVector = Icons.Filled.History, // You can replace this with your custom icon if needed
                        contentDescription = "Completed Tasks History"
                    )
                }
            }
            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }


    )
}

