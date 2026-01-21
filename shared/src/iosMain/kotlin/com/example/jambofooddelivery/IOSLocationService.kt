package com.example.jambofooddelivery

import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.repositories.PlatformLocationService
import com.example.jambofooddelivery.utils.Result
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IOSLocationService : PlatformLocationService, NSObject(), CLLocationManagerDelegateProtocol {

    private val locationManager = CLLocationManager()
    private var locationContinuation: ((Location?) -> Unit)? = null

    override suspend fun getCurrentLocation(): Location? {
        return suspendCoroutine { continuation ->
            locationContinuation = { location ->
                continuation.resume(location)
                locationContinuation = null
            }

            locationManager.delegate = this
            locationManager.requestWhenInUseAuthorization()
            locationManager.requestLocation()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
                clLocation?.let { location ->
                    location.coordinate.useContents {
                        trySend(Location(latitude, longitude))
                    }
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                // Handle error if needed
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }

    override suspend fun requestLocationPermission(): Boolean {
        locationManager.requestWhenInUseAuthorization()
        return hasLocationPermission()
    }

    @OptIn(ExperimentalForeignApi::class)
    // CLLocationManagerDelegateProtocol implementation for getCurrentLocation
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
        clLocation?.let { location ->
            location.coordinate.useContents {
                locationContinuation?.invoke(Location(latitude, longitude))
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        locationContinuation?.invoke(null)
    }
}





//class IOSLocationService : LocationRepository, NSObject(), CLLocationManagerDelegateProtocol {
//
//    private val locationManager = CLLocationManager()
//    private var locationContinuation: ((Location?) -> Unit)? = null
//
//    override suspend fun getCurrentLocation(): Location? {
//        return suspendCoroutine { continuation ->
//            locationContinuation = { location ->
//                continuation.resume(location)
//                locationContinuation = null
//            }
//
//            locationManager.delegate = this
//            locationManager.requestWhenInUseAuthorization()
//            locationManager.requestLocation()
//        }
//    }
//    @ExperimentalForeignApi
//    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
//        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
//            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
//                val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
//                clLocation?.let { location ->
//                    location.coordinate.useContents {
//                        trySend(Location(latitude, longitude))
//                    }
//                }
//            }
//
//            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
//                // Handle error if needed
//            }
//        }
//
//        locationManager.delegate = delegate
//        locationManager.requestWhenInUseAuthorization()
//        locationManager.startUpdatingLocation()
//
//        awaitClose {
//            locationManager.stopUpdatingLocation()
//        }
//    }
//
//    override suspend fun hasLocationPermission(): Boolean {
//        val status = CLLocationManager.authorizationStatus()
//        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
//                status == kCLAuthorizationStatusAuthorizedAlways
//    }
//
//    override suspend fun requestLocationPermission(): Boolean {
//        locationManager.requestWhenInUseAuthorization()
//        return hasLocationPermission()
//    }
//    @ExperimentalForeignApi
//    override suspend fun geocodeAddress(address: String): com.example.jambofooddelivery.utils.Result<Location> {
//        return suspendCoroutine { continuation ->
//            val geocoder = CLGeocoder()
//            geocoder.geocodeAddressString(address) { placemarks, error ->
//                if (error != null) {
//                    continuation.resume(com.example.jambofooddelivery.utils.Result.Error(error.localizedDescription))
//                } else {
//                    val placemark = placemarks?.firstOrNull() as? CLPlacemark
//                    val location = placemark?.location
//                    if (location != null) {
//                        location.coordinate.useContents {
//                            continuation.resume(com.example.jambofooddelivery.utils.Result.Success(Location(latitude, longitude)))
//                        }
//                    } else {
//                        continuation.resume(com.example.jambofooddelivery.utils.Result.Error("Location not found"))
//                    }
//                }
//            }
//        }
//    }
//
//    override suspend fun reverseGeocode(location: Location): com.example.jambofooddelivery.utils.Result<String> {
//        return suspendCoroutine { continuation ->
//            val geocoder = CLGeocoder()
//            val clLocation = CLLocation(latitude = location.latitude, longitude = location.longitude)
//            geocoder.reverseGeocodeLocation(clLocation) { placemarks, error ->
//                if (error != null) {
//                    continuation.resume(com.example.jambofooddelivery.utils.Result.Error(error.localizedDescription))
//                } else {
//                    val placemark = placemarks?.firstOrNull() as? CLPlacemark
//                    val addressString = listOfNotNull(
//                        placemark?.thoroughfare,
//                        placemark?.locality,
//                        placemark?.administrativeArea,
//                        placemark?.postalCode,
//                        placemark?.country
//                    ).joinToString(", ")
//
//                    if (addressString.isNotEmpty()) {
//                        continuation.resume(com.example.jambofooddelivery.utils.Result.Success(addressString))
//                    } else {
//                        continuation.resume(Result.Error("Address not found"))
//                    }
//                }
//            }
//        }
//    }
//    @ExperimentalForeignApi
//    // CLLocationManagerDelegateProtocol implementation for getCurrentLocation
//    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
//        val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
//        clLocation?.let { location ->
//            location.coordinate.useContents {
//                locationContinuation?.invoke(Location(latitude, longitude))
//            }
//        }
//    }
//
//    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
//        locationContinuation?.invoke(null)
//    }
//}
