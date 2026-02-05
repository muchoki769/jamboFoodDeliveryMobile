package com.jambofooddelivery.di

import com.jambofooddelivery.IOSLocationService
import com.jambofooddelivery.repositories.PlatformLocationService
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule: Module = module {
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }

    single<PlatformLocationService> { IOSLocationService() }
}
