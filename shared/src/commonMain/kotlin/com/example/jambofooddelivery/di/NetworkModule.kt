package com.example.jambofooddelivery.di

import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.ApiServiceImpl
import com.example.jambofooddelivery.remote.CloudinaryService
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient(get()) }
    single<ApiService> { ApiServiceImpl(get()) }
    single { CloudinaryService(get()) }
}

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient(appSettings: AppSettings): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }

        install(DefaultRequest) {
            url("https://jambofooddeliverybackend-754053186113.europe-west1.run.app/")
            header(HttpHeaders.ContentType, ContentType.Application.Json)

//            val token = appSettings.getToken()
//            if (!token.isNullOrBlank()) {
//                header(HttpHeaders.Authorization, "Bearer $token")
//            }
//        }
//
//        // Dynamic Token Injection for each request
//        install(io.ktor.client.plugins.auth.Auth) {
//             // Ktor 3.0+ syntax for bearer auth
//             // Using a simpler approach with interceptors if needed,
//             // but let's try a plugin if available or just use defaultRequest.
//             // Actually, defaultRequest headers are set at creation time for some plugins.
//             // Let's use an interceptor to ensure the latest token is used.
        }
//Added a createClientPlugin that intercepts every single outgoing request.
// It now fetches the latest token from appSettings immediately before the request is sent.
        install(createClientPlugin("AuthorizationHeader") {
            onRequest { request, _ ->
                val token = appSettings.getToken()
                if (!token.isNullOrBlank()) {
                    request.header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        })

        expectSuccess = false // Handle errors manually in ApiService
    }
}
