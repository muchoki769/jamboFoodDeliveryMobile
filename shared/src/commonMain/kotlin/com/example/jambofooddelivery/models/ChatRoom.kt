package com.example.jambofooddelivery.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class ChatRoom(
    val id: String,
    val name: String,
    val participants: List<String>,
    val lastMessage: ChatMessage?,
    val unreadCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val type: RoomType
)

@Serializable
enum class RoomType {
    SUPPORT, RESTAURANT, RIDER
}