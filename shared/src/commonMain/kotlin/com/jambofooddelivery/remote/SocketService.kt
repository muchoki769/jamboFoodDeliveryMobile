package com.jambofooddelivery.remote

import com.jambofooddelivery.models.ChatMessage
import com.jambofooddelivery.models.Location
import com.jambofooddelivery.models.Order
import dev.icerock.moko.socket.Socket
import dev.icerock.moko.socket.SocketEvent
import dev.icerock.moko.socket.SocketOptions
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SocketService {

    private var socket: Socket? = null
    private val _messageFlow = MutableSharedFlow<Pair<String, String>>()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun connect(userId: String) {
        if (socket != null) return

        socket = Socket(
            endpoint = "https://jambofooddeliverybackend-754053186113.europe-west1.run.app",
            config = SocketOptions(
                queryParams = mapOf("userId" to userId),
                transport = SocketOptions.Transport.WEBSOCKET
            )
        ) {
            on(SocketEvent.Connect) {
                Napier.d("SocketService: Connected to server")
            }

            on(SocketEvent.Disconnect) {
                Napier.d("SocketService: Disconnected from server")
            }

            on("new_message") { data ->
                Napier.d("SocketService: Received new_message: $data")
                scope.launch { _messageFlow.emit("new_message" to data) }
            }

            on("order-tracking-updated") { data ->
                Napier.d("SocketService: Received order-tracking-updated: $data")
                scope.launch { _messageFlow.emit("order-tracking-updated" to data) }
            }

            on("rider-location-updated") { data ->
                Napier.d("SocketService: Received rider-location-updated: $data")
                scope.launch { _messageFlow.emit("rider-location-updated" to data) }
            }
        }

        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join_room", roomId)
    }

    fun leaveRoom(roomId: String) {
        socket?.emit("leave_room", roomId)
    }

    fun sendMessage(roomId: String, userId: String, message: String) {
        val payload = mapOf(
            "room_id" to roomId,
            "sender_id" to userId,
            "message" to message,
            "message_type" to "text"
        )
        socket?.emit("send_message", Json.encodeToString(payload))
    }

    fun listenForChatMessages(roomId: String): Flow<ChatMessage> {
        return _messageFlow
            .filter { it.first == "new_message" }
            .map { it.second }
            .mapNotNull { data ->
                try {
                    Json.decodeFromString<ChatMessage>(data)
                } catch (e: Exception) {
                    Napier.e("SocketService: Error parsing ChatMessage", e)
                    null
                }
            }
            .filter { it.roomId == roomId }
    }

    fun listenForOrderUpdates(orderId: String): Flow<Order> {
        return _messageFlow
            .filter { it.first == "order-tracking-updated" }
            .map { it.second }
            .mapNotNull { data ->
                try {
                    Json.decodeFromString<Order>(data)
                } catch (e: Exception) {
                    Napier.e("SocketService: Error parsing Order", e)
                    null
                }
            }
            .filter { it.id == orderId }
    }

    fun listenForRiderLocation(orderId: String): Flow<Location> {
        return _messageFlow
            .filter { it.first == "rider-location-updated" }
            .map { it.second }
            .mapNotNull { data ->
                try {
                    Json.decodeFromString<Location>(data)
                } catch (e: Exception) {
                    Napier.e("SocketService: Error parsing Location", e)
                    null
                }
            }
    }
}