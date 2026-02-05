package com.jambofooddelivery.domain

import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.MessageType
import com.jambofooddelivery.repositories.ChatRepository
import com.jambofooddelivery.utils.Result

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