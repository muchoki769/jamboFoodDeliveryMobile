package com.example.jambofooddelivery.di
import com.example.jambofooddelivery.cache.Database
import com.example.jambofooddelivery.domain.*
import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.remote.SocketService
import com.example.jambofooddelivery.repositories.*
import com.example.jambofooddelivery.utils.AnalyticsService
import com.example.jambofooddelivery.utils.CloudinaryUploader
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module



expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    includes(networkModule)
    
    // Database
    single { Database(get()) }


    // Repositories
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    singleOf(::RestaurantRepositoryImpl) { bind<RestaurantRepository>() }
    singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }
    singleOf(::PaymentRepositoryImpl) { bind<PaymentRepository>() }
    singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    singleOf(::CartRepositoryImpl) { bind<CartRepository>() }
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


    // Settings
    singleOf(::AppSettings)

    //utils
    singleOf(::CloudinaryUploader)

    // ViewModels

}