package com.example.jambofooddelivery.preferences

import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.User
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppSettings(private val settings: Settings) {

    companion object {
        private const val KEY_USER = "user"
        private const val KEY_TOKEN = "token"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LOCATION_ENABLED = "location_enabled"
        private const val KEY_CACHED_LAT = "cached_lat"
        private const val KEY_CACHED_LNG = "cached_lng"
        private const val KEY_CACHED_ADDRESS = "cached_address"
    }

    fun saveUser(user: User) {
        settings[KEY_USER] = Json.encodeToString(user)
    }

    fun getCurrentUser(): User? {
        return try {
            settings.getStringOrNull(KEY_USER)?.let {
                Json.decodeFromString<User>(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveToken(token: String) {
        settings[KEY_TOKEN] = token
    }

    fun getToken(): String? {
        return settings.getStringOrNull(KEY_TOKEN)
    }

    fun clearUser() {
        settings.remove(KEY_USER)
    }

    fun clearToken() {
        settings.remove(KEY_TOKEN)
    }

    fun saveFcmToken(token: String) {
        settings[KEY_FCM_TOKEN] = token
    }

    fun getFcmToken(): String? {
        return settings.getStringOrNull(KEY_FCM_TOKEN)
    }

    fun setLocationEnabled(enabled: Boolean) {
        settings[KEY_LOCATION_ENABLED] = enabled
    }

    fun isLocationEnabled(): Boolean {
        return settings.getBoolean(KEY_LOCATION_ENABLED, false)
    }

    fun saveCachedLocation(location: Location, address: String?) {
        settings[KEY_CACHED_LAT] = location.latitude.toString()
        settings[KEY_CACHED_LNG] = location.longitude.toString()
        if (address != null) {
            settings[KEY_CACHED_ADDRESS] = address
        }
    }

    fun getCachedLocation(): Location? {
        val lat = settings.getStringOrNull(KEY_CACHED_LAT)?.toDoubleOrNull()
        val lng = settings.getStringOrNull(KEY_CACHED_LNG)?.toDoubleOrNull()
        val address = settings.getStringOrNull(KEY_CACHED_ADDRESS)
        
        return if (lat != null && lng != null) {
            Location(latitude = lat, longitude = lng, address = address)
        } else {
            null
        }
    }

    fun getCachedAddress(): String? {
        return settings.getStringOrNull(KEY_CACHED_ADDRESS)
    }
}

// Extension function for nullable string
private fun Settings.getStringOrNull(key: String): String? {
    return if (this.contains(key)) this[key] else null
}
