package com.jambofooddelivery

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform