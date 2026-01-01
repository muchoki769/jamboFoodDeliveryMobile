package com.example.jambofooddelivery

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform