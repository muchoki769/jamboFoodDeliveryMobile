package com.jambofooddelivery.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jambofooddelivery.domain.UpdateLocationUseCase
import com.jambofooddelivery.preferences.AppSettings
import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.repositories.LocationRepository
import com.jambofooddelivery.ui.components.ManualLocationDialog
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LocationPermissionScreen(
    onLocationGranted: () -> Unit,
    onSkip: () -> Unit
) {
    val locationRepository: LocationRepository = koinInject()
    val authRepository: AuthRepository = koinInject()
    val updateLocationUseCase: UpdateLocationUseCase = koinInject()
    val appSettings: AppSettings = koinInject()
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (granted) {
            scope.launch {
                isChecking = true
                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    // Get address for caching
                    val addressResult = locationRepository.reverseGeocode(location)
                    val addressStr = if (addressResult is Result.Success) addressResult.data else null
                    
                    // Save to persistent settings
                    appSettings.saveCachedLocation(location, addressStr)
                    
                    val user = authRepository.getCurrentUser().firstOrNull()
                    if (user != null) {
                        updateLocationUseCase(location, user.id)
                    }
                    isChecking = false
                    onLocationGranted()
                } else {
                    isChecking = false
                    errorMessage = "Could not detect location. Please try entering manually."
                }
            }
        } else {
            errorMessage = "Location permission denied"
        }
    }

    if (showManualDialog) {
        ManualLocationDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { address ->
                scope.launch {
                    isChecking = true
                    errorMessage = null
                    // Verify if it's a real location using geocoding
                    val result = locationRepository.geocodeAddress(address)
                    if (result is Result.Success) {
                        val location = result.data.copy(address = address)
                        
                        // Save to persistent settings
                        appSettings.saveCachedLocation(location, address)

                        val user = authRepository.getCurrentUser().firstOrNull()
                        if (user != null) {
                            updateLocationUseCase(location, user.id)
                        }
                        isChecking = false
                        showManualDialog = false
                        onLocationGranted()
                    } else {
                        isChecking = false
                        errorMessage = (result as? Result.Error)?.message ?: "Location not found"
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

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isChecking) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
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
