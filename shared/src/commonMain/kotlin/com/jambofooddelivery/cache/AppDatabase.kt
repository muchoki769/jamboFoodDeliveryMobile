package com.jambofooddelivery.cache



class Database(databaseDriverFactory: DatabaseDriverFactory) {
    val database = AppDatabase(databaseDriverFactory.createDriver())

    val usersQueries = database.userQueries
    val cartQueries = database.cartQueries
    val restaurantQueries = database.restaurantQueries
}
