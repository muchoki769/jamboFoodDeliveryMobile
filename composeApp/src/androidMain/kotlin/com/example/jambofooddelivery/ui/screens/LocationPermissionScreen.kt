package com.example.jambofooddelivery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jambofooddelivery.repositories.LocationRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LocationPermissionScreen(
    onLocationGranted: () -> Unit,
    onSkip: () -> Unit
) {
    val locationRepository: LocationRepository = koinInject()
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    if (showManualDialog) {
        ManualLocationDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { address ->
                scope.launch {
                    isChecking = true
                    // Verify if it's a real location using geocoding
                    val result = locationRepository.geocodeAddress(address)
                    isChecking = false
                    if (result is com.example.jambofooddelivery.utils.Result.Success) {
                        showManualDialog = false
                        onLocationGranted()
                    } else {
                        // Show error or Toast? For now just stay on dialog
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Enable Your Location",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please allow Jambo Food Delivery to access your location to find the best restaurants near you.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isChecking) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isChecking = true
                        val granted = locationRepository.requestLocationPermission()
                        isChecking = false
                        if (granted) {
                            onLocationGranted()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Allow Location Access")
            }
            
            TextButton(
                onClick = { showManualDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enter Location Manually")
            }
        }
    }
}
