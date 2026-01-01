package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.ChatMessage
import com.example.jambofooddelivery.models.MessageType
import com.example.jambofooddelivery.repositories.ChatRepository
import com.example.jambofooddelivery.utils.Result

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        roomId: String, 
        message: String, 
        type: MessageType = MessageType.TEXT
    ): Result<ChatMessage> {
        return chatRepository.sendMessage(roomId, message, type)
    }
}