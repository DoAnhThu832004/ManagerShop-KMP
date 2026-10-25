// desktopApp/src/main/kotlin/com/thupo/main.kt
package com.thupo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.thupo.managershop.di.appModule
import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger
import org.koin.core.logger.Level

fun main() {
    // Khởi tạo Koin trước khi tạo bất kỳ cửa sổ Compose nào
    startKoin {
        if (BuildKonfig.IS_DEBUG) {
            logger(PrintLogger(Level.INFO))
        } else {
            logger(PrintLogger(Level.NONE))
        }
        modules(appModule)
    }

    MonitoringManager.initMonitoring()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ManagerShop",
        ) {
            App()
        }
    }
}
