// shared/src/commonMain/kotlin/com/thupo/managershop/di/NetworkModule.kt
package com.thupo.managershop.di

import com.thupo.managershop.data.remote.HttpClientFactory
import com.thupo.managershop.data.remote.api.HealthApi
import com.thupo.managershop.data.remote.api.HealthApiImpl
import com.thupo.managershop.data.remote.createHttpEngine
import org.koin.dsl.module

/**
 * Koin module cung cấp các dependency tầng Network:
 * - HttpClientEngineFactory (Platform expect/actual)
 * - HttpClient (Ktor 3.x với cấu hình Timeout, Logging, Json)
 * - HealthApi (API warm-up và kiểm tra trạng thái máy chủ Render)
 */
val networkModule = module {
    single { createHttpEngine() }
    single { HttpClientFactory.create(engineFactory = get()) }
    single<HealthApi> { HealthApiImpl(httpClient = get()) }
}
