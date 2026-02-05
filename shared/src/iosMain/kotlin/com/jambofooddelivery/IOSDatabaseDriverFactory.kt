package com.jambofooddelivery

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jambofooddelivery.cache.AppDatabase



import com.jambofooddelivery.cache.DatabaseDriverFactory

//expect class IOSDatabaseDriverFactory : DatabaseDriverFactory {
//    override fun createDriver(): SqlDriver
//}






class IOSDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "jambofood.db")
    }
}




//actual class DatabaseDriverFactory {
//    actual fun createDriver(): SqlDriver {
//        return NativeSqliteDriver(
//            schema = AppDatabase.Schema,
//            name = "jambofood.db"
//        )
//    }
//}