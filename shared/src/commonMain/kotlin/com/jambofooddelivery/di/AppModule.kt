package com.jambofooddelivery.di
import com.jambofooddelivery.cache.Database
import com.jambofooddelivery.domain.*
import com.jambofooddelivery.preferences.AppSettings
import com.jambofooddelivery.remote.SocketService
import com.jambofooddelivery.repositories.*
import com.jambofooddelivery.utils.AnalyticsService
import com.jambofooddelivery.utils.CloudinaryUploader
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
    singleOf(::NotificationRepositoryImpl) { bind<NotificationRepository>() }

    // Use Cases
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::GetRestaurantsUseCase)
    singleOf(::SearchRestaurantsUseCase)
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
