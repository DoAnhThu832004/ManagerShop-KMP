// androidApp/src/main/kotlin/com/thupo/ShopApplication.kt
package com.thupo

import android.app.Application
import com.thupo.managershop.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ShopApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Chỉ ghi log INFO trong môi trường DEBUG; tắt hoàn toàn trong Release để tối ưu Cold Start
            if (BuildKonfig.IS_DEBUG) {
                androidLogger(Level.INFO)
            } else {
                androidLogger(Level.NONE)
            }
            // Cung cấp Android Context cho Koin
            androidContext(this@ShopApplication)
            // Nạp các module
            modules(appModule)
        }

        // Khởi tạo Sentry Crash Reporting bất đồng bộ để không chặn Main Thread và First Frame render
        applicationScope.launch {
            MonitoringManager.initMonitoring()
        }
    }
}
