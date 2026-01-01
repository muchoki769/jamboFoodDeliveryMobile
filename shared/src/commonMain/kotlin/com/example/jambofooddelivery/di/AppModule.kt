package com.example.jambofooddelivery.di
import com.example.jambofooddelivery.cache.AppDatabase
import com.example.jambofooddelivery.cache.DatabaseDriverFactory
import com.example.jambofooddelivery.domain.*
import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.ApiServiceImpl
import com.example.jambofooddelivery.remote.SocketService
import com.example.jambofooddelivery.repositories.*
import com.example.jambofooddelivery.utils.AnalyticsService
import com.example.jambofooddelivery.utils.CloudinaryUploader
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module



expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    // Database
    single { AppDatabase(get()) }
//    single { DatabaseDriverFactory(get()) }


    // Repositories
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    singleOf(::RestaurantRepositoryImpl) { bind<RestaurantRepository>() }
    singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }
    singleOf(::PaymentRepositoryImpl) { bind<PaymentRepository>() }
    singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }

    // Use Cases
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::GetRestaurantsUseCase)
    singleOf(::CreateOrderUseCase)
    singleOf(::TrackOrderUseCase)
    singleOf(::ProcessPaymentUseCase)
    singleOf(::SendMessageUseCase)
    singleOf(::UpdateLocationUseCase)

    // remote
    singleOf(::SocketService)
    singleOf(::AnalyticsService)
    single<ApiService> { ApiServiceImpl(get()) }


    // Settings
    singleOf(::AppSettings)

    //utils
    singleOf(::CloudinaryUploader)

    // ViewModels

}