package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.models.Notification
import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.repositories.NotificationRepository
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class NotificationState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null
)

class NotificationViewModel : BaseViewModel<NotificationState, Unit>(NotificationState()), KoinComponent {
    private val notificationRepository: NotificationRepository by inject()
    private val appSettings: AppSettings by inject()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        launch {
            setState { it.copy(isLoading = true, error = null) }
            val userId = appSettings.getCurrentUser()?.id
            if (userId == null) {
                setState { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }

            when (val result = notificationRepository.getNotifications(userId)) {
                is Result.Success -> {
                    setState { it.copy(isLoading = false, notifications = result.data) }
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun markAsRead(notificationId: String) {
        launch {
            notificationRepository.markAsRead(notificationId)
            // Optionally refresh or update local state
            val updatedList = state.value.notifications.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            setState { it.copy(notifications = updatedList) }
        }
    }
}
