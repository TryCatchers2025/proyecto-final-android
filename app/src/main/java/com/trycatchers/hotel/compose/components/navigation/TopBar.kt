package com.trycatchers.hotel.compose.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.trycatchers.hotel.compose.Screen

@Composable
fun TopBar(navigateTo: (Screen, String?) -> Unit, onThemeToggle: () -> Unit) {
    TopBarView(onThemeToggle = onThemeToggle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarView(onThemeToggle: (() -> Unit)? = null) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    TopAppBar(
        title = { Text(text = "Galactic Heaven", fontWeight = FontWeight.Bold) },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
        actions = {
            if (onThemeToggle != null) {
                IconButton(onClick = onThemeToggle) {
                    Icon(
                        imageVector =
                            if (isDarkTheme) Icons.Outlined.LightMode
                            else Icons.Outlined.DarkMode,
                        contentDescription = "Cambiar tema",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun TopBarPreview() {
    TopBarView()
}
