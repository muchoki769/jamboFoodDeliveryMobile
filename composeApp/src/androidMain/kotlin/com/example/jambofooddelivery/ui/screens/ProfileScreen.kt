package com.example.jambofooddelivery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jambofooddelivery.ui.ViewModels.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    if (state.showManualLocationDialog) {
        ManualLocationDialog(
            onDismiss = { viewModel.setShowManualLocationDialog(false) },
            onConfirm = { address -> viewModel.updateLocationManually(address) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            state.user?.let {
                Text("Name: ${it.fullName}", style = MaterialTheme.typography.titleMedium)
                Text("Email: ${it.email}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Location", style = MaterialTheme.typography.titleSmall)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = state.locationAddress ?: "No location set",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.updateLocationAuto() },
                                enabled = !state.isUpdatingLocation,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Auto Detect")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.setShowManualLocationDialog(true) },
                                enabled = !state.isUpdatingLocation,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Manual Entry")
                            }
                        }
                        
                        if (state.isUpdatingLocation) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ManualLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Location") },
        text = {
            Column {
                Text("Please enter your street address, city, or area name.")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("e.g. 123 Main St, Nairobi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (address.isNotBlank()) onConfirm(address) },
                enabled = address.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
