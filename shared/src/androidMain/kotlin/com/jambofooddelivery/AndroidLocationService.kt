package com.jambofooddelivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.jambofooddelivery.models.Location
import com.jambofooddelivery.repositories.PlatformLocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidLocationService(private val context: Context) : PlatformLocationService {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L
    ).build()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        return suspendCoroutine { continuation ->
            try {
                // Try last location first
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { androidLocation ->
                        if (androidLocation != null) {
                            continuation.resume(Location(androidLocation.latitude, androidLocation.longitude))
                        } else {
                            // If last location is null, request current location
                            val cancellationTokenSource = CancellationTokenSource()
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                cancellationTokenSource.token
                            ).addOnSuccessListener { freshLocation ->
                                if (freshLocation != null) {
                                    continuation.resume(Location(freshLocation.latitude, freshLocation.longitude))
                                } else {
                                    continuation.resume(null)
                                }
                            }.addOnFailureListener {
                                continuation.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { androidLocation ->
                    trySend(Location(androidLocation.latitude, androidLocation.longitude))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestLocationPermission(): Boolean {
        return false
    }
}
