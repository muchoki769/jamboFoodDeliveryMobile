package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.domain.UpdateLocationUseCase
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.User
import com.example.jambofooddelivery.repositories.AuthRepository
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.repositories.UserRepository
import com.example.jambofooddelivery.utils.Result
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class SettingsState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val userLocation: Location? = null,
    val locationAddress: String? = null,
    val isUpdatingLocation: Boolean = false,
    val showManualLocationDialog: Boolean = false,
    val error: String? = null
)

sealed class SettingsEvent {
    object LogoutSuccess : SettingsEvent()
    data class ShowError(val message: String) : SettingsEvent()
    object LocationUpdated : SettingsEvent()
}

class SettingsViewModel : BaseViewModel<SettingsState, SettingsEvent>(SettingsState()), KoinComponent {

    private val userRepository: UserRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val locationRepository: LocationRepository by inject()
    private val updateLocationUseCase: UpdateLocationUseCase by inject()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        launch {
            setState { it.copy(isLoading = true) }
            val user = userRepository.getCurrentUser()
            setState { it.copy(isLoading = false, user = user) }
        }
    }

    fun updateLocationAuto() {
        launch {
            setState { it.copy(isUpdatingLocation = true, error = null) }
            
            if (locationRepository.hasLocationPermission()) {
                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    val user = state.value.user
                    if (user != null) {
                        updateLocationUseCase(location, user.id)
                    }
                    
                    val addressResult = locationRepository.reverseGeocode(location)
                    setState { 
                        it.copy(
                            isUpdatingLocation = false,
                            userLocation = location,
                            locationAddress = when (addressResult) {
                                is Result.Success -> addressResult.data
                                else -> "${location.latitude}, ${location.longitude}"
                            }
                        )
                    }
                    emitEvent(SettingsEvent.LocationUpdated)
                } else {
                    setState { it.copy(isUpdatingLocation = false, error = "Could not get current location") }
                    emitEvent(SettingsEvent.ShowError("Could not get current location"))
                }
            } else {
                setState { it.copy(isUpdatingLocation = false, error = "Location permission denied") }
                emitEvent(SettingsEvent.ShowError("Location permission denied"))
            }
        }
    }

    fun setShowManualLocationDialog(show: Boolean) {
        setState { it.copy(showManualLocationDialog = show) }
    }

    fun updateLocationManually(address: String) {
        launch {
            setState { it.copy(isUpdatingLocation = true, error = null, showManualLocationDialog = false) }
            
            when (val result = locationRepository.geocodeAddress(address)) {
                is Result.Success -> {
                    val location = result.data
                    val user = state.value.user
                    if (user != null) {
                        updateLocationUseCase(location, user.id)
                    }

                    setState { 
                        it.copy(
                            isUpdatingLocation = false,
                            userLocation = location,
                            locationAddress = address
                        )
                    }
                    emitEvent(SettingsEvent.LocationUpdated)
                }
                is Result.Error -> {
                    setState { it.copy(isUpdatingLocation = false, error = result.message) }
                    emitEvent(SettingsEvent.ShowError(result.message))
                }
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        launch {
            authRepository.logout()
            emitEvent(SettingsEvent.LogoutSuccess)
        }
    }
}
