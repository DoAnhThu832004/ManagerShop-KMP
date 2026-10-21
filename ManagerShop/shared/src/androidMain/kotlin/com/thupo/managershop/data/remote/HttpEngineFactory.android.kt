// shared/src/androidMain/kotlin/com/thupo/managershop/data/remote/HttpEngineFactory.android.kt
package com.thupo.managershop.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpEngine(): HttpClientEngineFactory<*> = OkHttp
