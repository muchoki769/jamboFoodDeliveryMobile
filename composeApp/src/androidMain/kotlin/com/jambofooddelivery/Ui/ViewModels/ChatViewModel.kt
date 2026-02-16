package com.jambofooddelivery.Ui.ViewModels

import com.jambofooddelivery.domain.SendMessageUseCase
import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.ChatRoom
import com.jambofooddelivery.models.MessageType
import com.jambofooddelivery.remote.SocketService
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
    val currentUserId: String = "",
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
    private val socketService: SocketService by inject()

    private var messageListeningJob: Job? = null

    fun onMessageChange(message: String) {
        setState { it.copy(newMessage = message) }
    }

    fun openSupportChat(orderId: String? = null) {
        launch {
            setState { it.copy(isLoading = true, error = null) }
            val user = authRepository.getCurrentUser().first() ?: return@launch
            setState { it.copy(currentUserId = user.id) }
            
            // Connect to socket if not already connected
            socketService.connect(user.id)
            
            when (val result = chatRepository.getOrCreateSupportRoom(user.id, orderId)) {
                is Result.Success -> {
                    val room = result.data
                    setState { it.copy(currentRoom = room) }
                    
                    // 1. Load history
                    loadMessages(room.id)
                    
                    // 2. Join WS room
                    socketService.joinRoom(room.id)
                    
                    // 3. Listen for live messages
                    listenForMessages(room.id)
                    
                    setState { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun listenForMessages(roomId: String) {
        messageListeningJob?.cancel()
        messageListeningJob = launch {
            chatRepository.listenForNewMessages(roomId).collect { message ->
                // Avoid duplicates if message was already added by sendMessage locally or via API response
                if (!state.value.messages.any { it.id == message.id }) {
                    setState { it.copy(messages = it.messages + message) }
                }
                emitEvent(ChatEvent.NewMessageReceived(message))
            }
        }
    }

    fun sendMessage() {
        val content = state.value.newMessage
        val room = state.value.currentRoom ?: return
        val userId = state.value.currentUserId
        if (content.isBlank()) return

        launch {
            setState { it.copy(isSending = true) }
            // We use the Repository to save it to DB via API
            // The Repository now also handles Socket broadcast
            when (val result = chatRepository.sendMessage(room.id, userId, content, MessageType.TEXT)) {
                is Result.Success -> {
                    setState { it.copy(isSending = false, newMessage = "") }
                    // Message will come back via Socket listener to be added to UI
                }
                is Result.Error -> {
                    setState { it.copy(isSending = false) }
                    emitEvent(ChatEvent.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private suspend fun loadMessages(roomId: String) {
        when (val result = chatRepository.getChatMessages(roomId)) {
            is Result.Success -> setState { it.copy(messages = result.data) }
            else -> {}
        }
    }

    fun loadChatRooms() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            val user = authRepository.getCurrentUser().first()
            user?.let { userModel ->
                setState { it.copy(currentUserId = userModel.id) }
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

    override fun onCleared() {
        messageListeningJob?.cancel()
        socketService.disconnect()
        super.onCleared()
    }
}