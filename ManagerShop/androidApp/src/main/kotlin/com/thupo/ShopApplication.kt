// androidApp/src/main/kotlin/com/thupo/ShopApplication.kt
package com.thupo

import android.app.Application
import com.thupo.managershop.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ShopApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Ghi log Koin trên Logcat (chỉ DEBUG)
            androidLogger(Level.DEBUG)
            // Cung cấp Android Context cho Koin
            androidContext(this@ShopApplication)
            // Nạp các module
            modules(appModule)
        }
    }
}
