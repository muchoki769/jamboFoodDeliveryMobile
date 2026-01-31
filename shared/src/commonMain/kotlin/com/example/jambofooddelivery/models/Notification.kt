package com.example.jambofooddelivery.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant



import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
@Serializable
data class Notification(
    val id: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val title: String,
    val message: String,
    val type: String, // e.g., "order_update", "promo", "system"
    val metadata: Map<String, String>? = null,
    val isRead: Boolean = false
)