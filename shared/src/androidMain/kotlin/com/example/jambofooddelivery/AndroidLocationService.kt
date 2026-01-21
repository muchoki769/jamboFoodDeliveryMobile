package com.example.jambofooddelivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.repositories.PlatformLocationService
import com.example.jambofooddelivery.utils.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { androidLocation ->
                        androidLocation?.let {
                            continuation.resume(Location(it.latitude, it.longitude))
                        } ?: continuation.resume(null)
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
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
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
        // Cannot request permission from Service/Repository directly without Activity context/launcher
        return false
    }
}


//class AndroidLocationService(private val context: Context) : LocationRepository {
//    private val fusedLocationClient: FusedLocationProviderClient by lazy {
//        LocationServices.getFusedLocationProviderClient(context)
//    }
//
//    private val locationRequest = LocationRequest.Builder(
//        Priority.PRIORITY_HIGH_ACCURACY,
//        10000L
//    ).build()
//
//    @SuppressLint("MissingPermission")
//    override suspend fun getCurrentLocation(): Location? {
//        if (!hasLocationPermission()) {
//            return null
//        }
//        return suspendCoroutine { continuation ->
//            try {
//                fusedLocationClient.lastLocation
//                    .addOnSuccessListener { androidLocation ->
//                        androidLocation?.let {
//                            continuation.resume(Location(it.latitude, it.longitude))
//                        } ?: continuation.resume(null)
//                    }
//                    .addOnFailureListener {
//                        continuation.resume(null)
//                    }
//            } catch (e: SecurityException) {
//                continuation.resume(null)
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
//        // Check permissions first (though this flow is cold, the collector should probably check permissions too)
//        // Since hasLocationPermission is suspend, we can't call it directly in callbackFlow builder easily without launch.
//        // However, standard pattern is to assume permissions are granted before calling or check with non-suspend if possible.
//        // For simplicity, we'll use a non-suspend check or just launch a coroutine.
//
//        val hasPermission = ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//
//        if (!hasPermission) {
//            close()
//            return@callbackFlow
//        }
//
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.locations.lastOrNull()?.let { androidLocation ->
//                    trySend(Location(androidLocation.latitude, androidLocation.longitude))
//                }
//            }
//        }
//
//        try {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                null
//            )
//        } catch (e: SecurityException) {
//            close(e)
//        }
//
//        awaitClose {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    override suspend fun hasLocationPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    // Implementing missing methods from LocationRepository
//    override suspend fun requestLocationPermission(): Boolean {
//        // Cannot request permission from Service/Repository directly without Activity context/launcher
//        // This should ideally be handled in the UI layer or via a PermissionManager
//        return false
//    }
//
//    override suspend fun geocodeAddress(address: String): com.example.jambofooddelivery.utils.Result<Location> {
//        // Not implemented for AndroidLocationService directly, maybe delegate to Geocoder or API
//        return com.example.jambofooddelivery.utils.Result.Error("Not implemented")
//    }
//
//    override suspend fun reverseGeocode(location: Location): com.example.jambofooddelivery.utils.Result<String> {
//        // Not implemented for AndroidLocationService directly
//        return Result.Error("Not implemented")
//    }
//}
