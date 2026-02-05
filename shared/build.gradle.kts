import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }


    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        commonMain.dependencies {

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            // Ktor Client
            implementation("io.ktor:ktor-client-core:2.3.4")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
            implementation("io.ktor:ktor-client-logging:2.3.4")
            implementation("io.ktor:ktor-client-auth:2.3.4")

            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

            // Kotlinx Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            
            // SQLDelight
            implementation(libs.runtime)
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

            // Koin DI
            implementation("io.insert-koin:koin-core:3.4.3")


            // russhwolf
            implementation("com.russhwolf:multiplatform-settings:1.1.1")
            implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")

            // Logger
            implementation("io.github.aakira:napier:2.6.1")

            // Cloudinary URL Gen (Common)
            implementation("com.cloudinary:kotlin-url-gen:1.7.0")

        }
        androidMain.dependencies {
            implementation(libs.tehras.charts)

            // Ktor Android
            implementation("io.ktor:ktor-client-okhttp:2.3.4")

            //koin
            implementation("io.insert-koin:koin-android:3.5.6")

            // SQLDelight Android Driver
            implementation(libs.android.driver)

            
            // Location Services
            implementation("com.google.android.gms:play-services-location:21.0.1")

            // Firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.3.1"))
            implementation("com.google.firebase:firebase-analytics")
            implementation("com.google.firebase:firebase-crashlytics")
            implementation("com.google.firebase:firebase-messaging")

            // Paystack Android SDK
            implementation(libs.paystack.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
    }

}



android {
    namespace = "com.jambofooddelivery.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.jambofooddelivery.cache")
        }
    }
}
