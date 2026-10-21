// shared/src/commonMain/kotlin/com/thupo/managershop/di/NetworkModule.kt
package com.thupo.managershop.di

import com.thupo.managershop.data.remote.HttpClientFactory
import com.thupo.managershop.data.remote.api.AuthApi
import com.thupo.managershop.data.remote.api.AuthApiImpl
import com.thupo.managershop.data.remote.api.HealthApi
import com.thupo.managershop.data.remote.api.HealthApiImpl
import com.thupo.managershop.data.remote.createHttpEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module cung cấp các dependency tầng Network:
 * - HttpClientEngineFactory (Platform expect/actual)
 * - refreshHttpClient: HttpClient riêng biệt không cài Auth plugin, dùng cho Token Refresh
 * - HttpClient chính: Đã cài Auth Bearer Plugin với Mutex double-checking
 * - HealthApi & AuthApi
 */
val networkModule = module {
    single { createHttpEngine() }

    // HttpClient phụ dùng riêng cho POST auth/refresh (chống lặp vô tận)
    single(named("refreshHttpClient")) {
        HttpClientFactory.createRefreshClient(
            engineFactory = get(),
        )
    }

    // HttpClient chính tích hợp Auth Bearer Plugin + Mutex
    single {
        HttpClientFactory.createAuthenticatedClient(
            engineFactory = get(),
            tokenManager = get(),
            refreshClient = get(named("refreshHttpClient")),
        )
    }

    single<HealthApi> { HealthApiImpl(httpClient = get()) }
    single<AuthApi> { AuthApiImpl(httpClient = get()) }
}
