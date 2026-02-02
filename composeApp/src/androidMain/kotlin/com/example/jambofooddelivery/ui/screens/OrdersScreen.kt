package com.example.jambofooddelivery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jambofooddelivery.ui.ViewModels.ProfileViewModel
import com.example.jambofooddelivery.ui.components.BottomNavigationBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = "orders",
                onHomeClick = onNavigateToHome,
                onOrdersClick = { /* Already here */ },
                onProfileClick = onNavigateToProfile,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(state.orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    onClick = { onTrackOrder(order.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order #${order.orderNumber}")
                        Text("Status: ${order.status}")
                        Text("Total: $${order.totalAmount}")
                    }
                }
            }
        }
    }
}
