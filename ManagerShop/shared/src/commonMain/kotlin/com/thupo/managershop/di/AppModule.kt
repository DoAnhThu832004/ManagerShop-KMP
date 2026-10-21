// shared/src/commonMain/kotlin/com/thupo/managershop/di/AppModule.kt
package com.thupo.managershop.di

import com.thupo.managershop.core.AppDispatchers
import com.thupo.managershop.core.DefaultAppDispatchers
import org.koin.dsl.module

/**
 * Koin module cốt lõi của ứng dụng.
 * Đăng ký các singleton dùng chung trên cả Android và Desktop.
 *
 * Các feature module sẽ khai báo riêng và được nạp cùng module này
 * khi khởi tạo Koin ở mỗi entry point.
 */
val appModule = module {

    // --- Core ---
    single<AppDispatchers> { DefaultAppDispatchers() }
}
