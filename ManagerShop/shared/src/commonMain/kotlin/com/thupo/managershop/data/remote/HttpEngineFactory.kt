// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/HttpEngineFactory.kt
package com.thupo.managershop.data.remote

import io.ktor.client.engine.HttpClientEngineFactory

/**
 * expect fun cung cấp HttpClientEngineFactory cho từng nền tảng:
 * - Android: OkHttp
 * - Desktop JVM: OkHttp
 */
expect fun createHttpEngine(): HttpClientEngineFactory<*>
