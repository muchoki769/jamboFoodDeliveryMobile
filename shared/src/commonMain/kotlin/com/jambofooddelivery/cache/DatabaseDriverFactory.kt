package com.jambofooddelivery.cache

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

//expect class DatabaseDriverFactory {
//    fun createDriver(): SqlDriver
//}