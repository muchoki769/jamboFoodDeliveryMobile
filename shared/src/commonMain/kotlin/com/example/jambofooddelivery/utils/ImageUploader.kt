package com.example.jambofooddelivery.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class CloudinaryUploader(
    private val httpClient: HttpClient // Inject Ktor Client here
) {

    private val cloudName = "dahss2ggd"
    private val uploadPreset = "your-upload-preset" // Required for unsigned uploads

    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            val response: JsonObject = httpClient.post("https://api.cloudinary.com/v1_1/$cloudName/image/upload") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", imageBytes, Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    "image/jpeg"
                                ) // Or detect type dynamically
                                append(HttpHeaders.ContentDisposition, "filename=\"upload.jpg\"")
                            })
                            append("upload_preset", uploadPreset)
                        }
                    )
                )
            }.body()

            // Extract the secure URL from the JSON response
            response["secure_url"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
