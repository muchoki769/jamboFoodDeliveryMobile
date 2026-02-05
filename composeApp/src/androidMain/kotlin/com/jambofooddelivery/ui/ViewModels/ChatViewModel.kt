package com.jambofooddelivery.ui.ViewModels

import com.jambofooddelivery.domain.SendMessageUseCase
import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.ChatRoom
import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.repositories.ChatRepository
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ChatState(
    val isLoading: Boolean = false,
    val chatRooms: List<ChatRoom> = emptyList(),
    val currentRoom: ChatRoom? = null,
    val messages: List<ChatMessage> = emptyList(),
    val newMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val supportAgentsOnline: Boolean = false
)

sealed class ChatEvent {
    data class NewMessageReceived(val message: ChatMessage) : ChatEvent()
    data class ShowError(val message: String) : ChatEvent()
    object MessageSent : ChatEvent()
}

class ChatViewModel : BaseViewModel<ChatState, ChatEvent>(ChatState()), KoinComponent {

    private val sendMessageUseCase: SendMessageUseCase by inject()
    private val chatRepository: ChatRepository by inject()
    private val authRepository: AuthRepository by inject()

    private var messageListeningJob: Job? = null

    fun loadChatRooms() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            val user = authRepository.getCurrentUser().first()
            user?.let { userModel ->
                when (val result = chatRepository.getChatRooms(userModel.id)) {
                    is Result.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val chatRooms = result.data as List<ChatRoom>
                        setState {
                            it.copy(
                                isLoading = false,
                                chatRooms = chatRooms
                            )
                        }
                    }
                    is Result.Error -> {
                        setState {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        emitEvent(ChatEvent.ShowError(result.message))
                    }
                    is Result.Loading -> {
                        // Handle loading state if needed, though already set at start
                    }
                }
            }
        }
    }
}