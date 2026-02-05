package com.jambofooddelivery.models

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

//@Serializable
//data class ChatRoom(
//    val id: String,
//    @SerialName("order_id")
//    val orderId: String,
//    @SerialName("customer_id")    val customerId: String,
//    @SerialName("rider_id")
//    val riderId: String? = null,
//    @SerialName("last_message")
//    val lastMessage: String? = null,
//    @SerialName("last_message_timestamp")
//    val lastMessageTimestamp: Instant? = null,
//    @SerialName("unread_count")
//    val unreadCount: Int = 0,
//    @SerialName("created_at")
//    val createdAt: Instant,
//    @SerialName("updated_at")
//    val updatedAt: Instant
//)
@Serializable
enum class RoomType {
    SUPPORT, RESTAURANT, RIDER
}