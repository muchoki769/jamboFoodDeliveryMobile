package com.jambofooddelivery.repositories

import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.ChatRoom
import com.jambofooddelivery.models.MessageType
import com.jambofooddelivery.remote.ApiService
import com.jambofooddelivery.remote.CloudinaryService
import com.jambofooddelivery.remote.SendMessageRequest
import com.jambofooddelivery.remote.SocketService
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow


interface ChatRepository {
    suspend fun getChatRooms(userId: String): Result<List<ChatRoom>>
    suspend fun getOrCreateSupportRoom(userId: String, orderId: String? = null): Result<ChatRoom>
    suspend fun getChatMessages(roomId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(roomId: String, senderId: String, message: String, type: MessageType): Result<ChatMessage>
    suspend fun sendImageMessage(roomId: String, senderId: String, imageBytes: ByteArray): Result<ChatMessage>
    fun listenForNewMessages(roomId: String): Flow<ChatMessage>
    suspend fun markMessagesAsRead(roomId: String)
}

class ChatRepositoryImpl(
    private val apiService: ApiService,
    private val socketService: SocketService,
    private val cloudinaryService: CloudinaryService
) : ChatRepository {
    override suspend fun getChatRooms(userId: String): Result<List<ChatRoom>> {
        return try {
            val response = apiService.getChatRooms(userId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load chat rooms")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load chats: ${e.message}")
        }
    }

    override suspend fun getOrCreateSupportRoom(userId: String, orderId: String?): Result<ChatRoom> {
        return try {
            val response = apiService.getOrCreateSupportRoom(userId, orderId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to create chat room")
            }
        } catch (e: Exception) {
            Result.Error("Chat room creation failed: ${e.message}")
        }
    }

    override suspend fun getChatMessages(roomId: String): Result<List<ChatMessage>> {
        return try {
            val response = apiService.getChatMessages(roomId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load messages")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load messages: ${e.message}")
        }
    }

    override suspend fun sendMessage(roomId: String, senderId: String, message: String, type: MessageType): Result<ChatMessage> {
        return try {
            // First send via Socket for real-time delivery
            if (type == MessageType.TEXT) {
                socketService.sendMessage(roomId, senderId, message)
            }

            // Then persist in DB
            val response = apiService.sendMessage(
                SendMessageRequest(roomId, message, type)
            )
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to send message")
            }
        } catch (e: Exception) {
            Result.Error("Message sending failed: ${e.message}")
        }
    }

    override suspend fun sendImageMessage(roomId: String, senderId: String, imageBytes: ByteArray): Result<ChatMessage> {
        return try {
            val imageUrl = cloudinaryService.uploadImage(imageBytes)
            if (imageUrl != null) {
                sendMessage(roomId, senderId, imageUrl, MessageType.IMAGE)
            } else {
                Result.Error("Failed to upload image")
            }
        } catch (e: Exception) {
            Result.Error("Image upload failed: ${e.message}")
        }
    }

    override fun listenForNewMessages(roomId: String): Flow<ChatMessage> {
        return socketService.listenForChatMessages(roomId)
    }

    override suspend fun markMessagesAsRead(roomId: String) {
        // Implementation to mark messages as read
        apiService.markMessagesAsRead(roomId)
    }
}