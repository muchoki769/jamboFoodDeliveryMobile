package com.jambofooddelivery.remote

import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.Location
import com.jambofooddelivery.models.Order
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



class SocketService(private val client: HttpClient) {

    private var session: DefaultClientWebSocketSession? = null
    private val messageFlow = MutableSharedFlow<String>()

    suspend fun connect(token: String) {
        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = "https://bank-backend-754053186113.europe-west1.run.app",
                path = "/ws"
            ) {
                // Send authentication
                send(Json.encodeToString(mapOf("token" to token)))

                session = this

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            messageFlow.emit(text)
                        }
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            // Handle connection error
        }
    }

    suspend fun disconnect() {
        session?.close()
        session = null
    }

    suspend fun send(event: String, data: Any) {
        val message = Json.encodeToString(mapOf("event" to event, "data" to data))
        session?.send(message)
    }

    fun listen(event: String): Flow<String> {
        return messageFlow
            .filter { message ->
                try {
                    val json = Json.decodeFromString<Map<String, String>>(message)
                    json["event"] == event
                } catch (e: Exception) {
                    false
                }
            }
            .map { message ->
                val json = Json.decodeFromString<Map<String, String>>(message)
                json["data"] ?: ""
            }
    }

    fun listenForOrderUpdates(orderId: String): Flow<Order> {
        return listen("order-update-$orderId")
            .map { data ->
                Json.decodeFromString<Order>(data)
            }
    }

    fun listenForChatMessages(roomId: String): Flow<ChatMessage> {
        return listen("chat-message-$roomId")
            .map { data ->
                Json.decodeFromString<ChatMessage>(data)
            }
    }

    fun listenForRiderLocation(orderId: String): Flow<Location> {
        return listen("rider-location-$orderId")
            .map { data ->
                Json.decodeFromString<Location>(data)
            }
    }
}