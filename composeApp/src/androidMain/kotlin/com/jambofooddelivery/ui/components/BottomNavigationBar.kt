package com.jambofooddelivery.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = onHomeClick,
            icon = {
                Icon(Icons.Outlined.Home, contentDescription = "Home")
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedRoute == "orders",
            onClick = onOrdersClick,
            icon = {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders")
            },
            label = { Text("Orders") }
        )

        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(Icons.Outlined.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") }
        )

        NavigationBarItem(
            selected = selectedRoute == "settings",
            onClick = onSettingsClick,
            icon = {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
            },
            label = { Text("Settings") }
        )
    }
}
