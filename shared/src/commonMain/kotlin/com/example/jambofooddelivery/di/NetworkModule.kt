package com.example.jambofooddelivery.di

import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.ApiServiceImpl
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single<ApiService> { ApiServiceImpl(get()) }
}

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient(): HttpClient {
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

        defaultRequest {
            url(" https://jambofooddelivery.onrender.com/")

//            https://mini-elearning-web-backend.onrender.com/api/
        }

        expectSuccess = true
    }
}