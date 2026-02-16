package com.jambofooddelivery.Ui.ViewModels

import com.jambofooddelivery.domain.UpdateLocationUseCase
import com.jambofooddelivery.models.Location
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.models.ProfileUpdate
import com.jambofooddelivery.models.User
import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.repositories.LocationRepository
import com.jambofooddelivery.repositories.OrderRepository
import com.jambofooddelivery.repositories.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.jambofooddelivery.utils.Result

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val orders: List<Order> = emptyList(),
    val isEditing: Boolean = false,
    val editedUser: User? = null,
    val error: String? = null,
    val isUploadingImage: Boolean = false,
    val userLocation: Location? = null,
    val locationAddress: String? = null,
    val isUpdatingLocation: Boolean = false,
    val showManualLocationDialog: Boolean = false
)

sealed class ProfileEvent {
    object LogoutSuccess : ProfileEvent()
    data class ProfileUpdated(val user: User) : ProfileEvent()
    data class ShowError(val message: String) : ProfileEvent()
    data class ImageUploaded(val imageUrl: String) : ProfileEvent()
    object LocationUpdated : ProfileEvent()
}

class ProfileViewModel : BaseViewModel<ProfileState, ProfileEvent>(ProfileState()), KoinComponent {

    private val userRepository: UserRepository by inject()
    private val orderRepository: OrderRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val locationRepository: LocationRepository by inject()
    private val updateLocationUseCase: UpdateLocationUseCase by inject()

    init {
        launch {
            loadUserProfile()
            loadUserOrders()
        }
    }

    suspend fun loadUserProfile() {
        setState { it.copy(isLoading = true, error = null) }
        val user = userRepository.getCurrentUser()
        setState {
            it.copy(
                isLoading = false,
                user = user,
                editedUser = user
            )
        }
    }

    fun loadUserOrders() {
        launch {
            setState { it.copy(isLoading = true, error = null) }
            
            val currentUser = state.value.user ?: userRepository.getCurrentUser()
            
            if (currentUser == null) {
                setState { it.copy(isLoading = false, error = "User session not found") }
                return@launch
            }

            when (val result = orderRepository.getUserOrders(currentUser.id)) {
                is Result.Success -> {
                    val sortedOrders = result.data
                        .sortedByDescending { order -> order.createdAt }
                        .take(10)
                    setState { it.copy(orders = sortedOrders, isLoading = false) }
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {
                    setState { it.copy(isLoading = true) }
                }
            }
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
                    emitEvent(ProfileEvent.LocationUpdated)
                } else {
                    setState { it.copy(isUpdatingLocation = false, error = "Could not get current location") }
                    emitEvent(ProfileEvent.ShowError("Could not get current location"))
                }
            } else {
                val granted = locationRepository.requestLocationPermission()
                if (granted) {
                    updateLocationAuto() // Retry if granted
                } else {
                    setState { it.copy(isUpdatingLocation = false, error = "Location permission denied") }
                    emitEvent(ProfileEvent.ShowError("Location permission denied"))
                }
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
                    emitEvent(ProfileEvent.LocationUpdated)
                }
                is Result.Error -> {
                    setState { it.copy(isUpdatingLocation = false, error = result.message) }
                    emitEvent(ProfileEvent.ShowError(result.message))
                }
                is Result.Loading -> {}
            }
        }
    }

    fun startEditing() {
        setState { it.copy(isEditing = true, editedUser = state.value.user) }
    }

    fun cancelEditing() {
        setState { it.copy(isEditing = false, editedUser = state.value.user) }
    }

    fun updateProfile(updates: ProfileUpdate) {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            when (val result = userRepository.updateProfile(updates)) {
                is Result.Success -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            isEditing = false,
                            user = result.data,
                            editedUser = result.data
                        )
                    }
                    emitEvent(ProfileEvent.ProfileUpdated(result.data))
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(ProfileEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                    // Already set via setState
                }
            }
        }
    }

    fun uploadProfileImage(imageBytes: ByteArray) {
        launch {
            setState { it.copy(isUploadingImage = true, error = null) }

            when (val result = userRepository.uploadProfileImage(imageBytes)) {
                is Result.Success -> {
                    setState { it.copy(isUploadingImage = false) }
                    emitEvent(ProfileEvent.ImageUploaded(result.data))
                    // Reload user profile to get updated image
                    launch { loadUserProfile() }
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isUploadingImage = false,
                            error = result.message
                        )
                    }
                    emitEvent(ProfileEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                    // Already set via setState
                }
            }
        }
    }

    fun logout() {
        launch {
            authRepository.logout()
            emitEvent(ProfileEvent.LogoutSuccess)
        }
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }

}
