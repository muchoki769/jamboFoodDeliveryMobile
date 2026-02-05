package com.jambofooddelivery.remote

import com.jambofooddelivery.models.CloudinaryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json

class CloudinaryService(private val client: HttpClient) {

    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            val response: String =
                client.post("https://api.cloudinary.com/v1_1/your-cloud-name/image/upload") {
                    parameter("upload_preset", "your_upload_preset")
                    setBody(imageBytes)
                }.body()

            // Parse response to get URL
            val jsonResponse = Json.decodeFromString<CloudinaryResponse>(response)
            jsonResponse.secure_url
        } catch (e: Exception) {
            null
        }
    }
        suspend fun uploadMultipleImages(images: List<ByteArray>): List<String> {
            val urls = mutableListOf<String>()
            for (image in images) {
                uploadImage(image)?.let { urls.add(it) }
            }
            return urls
        }
    }
