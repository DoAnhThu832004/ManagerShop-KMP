// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/HttpClientFactory.kt
package com.thupo.managershop.data.remote

import com.thupo.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory khởi tạo Ktor [HttpClient] 3.x đa nền tảng.
 *
 * Cấu hình:
 * - Base URL: [BuildKonfig.BASE_URL] (https://manager-shop.onrender.com/api/v1/)
 * - ContentNegotiation: Kotlinx Serialization Json (ignoreUnknownKeys, explicitNulls=false)
 * - HttpTimeout: 60s cho connect, request, socket (hỗ trợ Render cold start)
 * - Logging: Sanitize Header `Authorization`, mask mật khẩu trong log body
 * - expectSuccess = true: Ném ResponseException khi gặp lỗi HTTP để [safeApiCall] bắt và xử lý
 */
object HttpClientFactory {

    private val passwordRegex = Regex("(\"password\"\\s*:\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE)

    fun create(engineFactory: HttpClientEngineFactory<*>): HttpClient {
        return HttpClient(engineFactory) {
            // Tự động ném ResponseException (ClientRequestException / ServerResponseException) khi status >= 300
            expectSuccess = true

            // Base URL cho toàn bộ request
            defaultRequest {
                url(BuildKonfig.BASE_URL)
            }

            // JSON Serialization
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }

            // Timeout 60s đối phó với Render Free Tier cold start (30-50s)
            install(HttpTimeout) {
                connectTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }

            // HTTP Logging (chỉ kích hoạt chi tiết khi DEBUG, che giấu dữ liệu nhạy cảm)
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        val sanitizedMessage = message.replace(passwordRegex, "$1***$2")
                        Logger.DEFAULT.log(sanitizedMessage)
                    }
                }
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.INFO

                // Ẩn token trong Authorization header
                sanitizeHeader { header ->
                    header.equals(HttpHeaders.Authorization, ignoreCase = true)
                }
            }
        }
    }
}
