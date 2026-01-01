package com.example.jambofooddelivery.di



import com.example.jambofooddelivery.IOSLocationService
import org.koin.core.module.Module
import org.koin.dsl.module

//actual val platformIOSModule: Module = module {
//    // This is where you add iOS-specific Koin dependencies.
//     single { IOSLocationService() }
//}



import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual val platformModule: Module = module {
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}
