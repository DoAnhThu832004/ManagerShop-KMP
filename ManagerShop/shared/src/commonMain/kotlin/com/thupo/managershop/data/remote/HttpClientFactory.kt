// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/HttpClientFactory.kt
package com.thupo.managershop.data.remote

import com.thupo.BuildKonfig
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import com.thupo.managershop.data.remote.dto.RefreshTokenRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
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
 * - Auth Bearer Plugin: Tự động đính kèm Access Token, Mutex double-checking khi refresh token 401
 */
object HttpClientFactory {

    private val passwordRegex = Regex("(\"password\"\\s*:\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE)
    private val refreshMutex = Mutex()

    private fun configureBase(config: HttpClientConfig<*>) {
        config.expectSuccess = true

        config.defaultRequest {
            url(BuildKonfig.BASE_URL)
        }

        config.install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }

        config.install(HttpTimeout) {
            connectTimeoutMillis = 60_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }

        if (BuildKonfig.IS_DEBUG) {
            config.install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        val sanitizedMessage = if (message.contains("password", ignoreCase = true)) {
                            message.replace(passwordRegex, "$1***$2")
                        } else {
                            message
                        }
                        Logger.DEFAULT.log(sanitizedMessage)
                    }
                }
                level = LogLevel.ALL

                sanitizeHeader { header ->
                    header.equals(HttpHeaders.Authorization, ignoreCase = true)
                }
            }
        }
    }

    /**
     * Cài đặt Auth Bearer Plugin với Mutex chống gọi song song
     * và cơ chế Double-Check Lock để đảm bảo an toàn tuyệt đối với Token Rotation.
     */
    private fun configureAuth(
        config: HttpClientConfig<*>,
        tokenManager: TokenManager,
        refreshClient: HttpClient,
    ) {
        config.install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenManager.getAccessToken()
                    val refreshToken = tokenManager.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                        )
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val failedRefreshToken = oldTokens?.refreshToken

                    refreshMutex.withLock {
                        val currentRefreshToken = tokenManager.getRefreshToken()
                        val currentAccessToken = tokenManager.getAccessToken()

                        // Double-Check Lock:
                        // Nếu refresh token trong storage đã khác với token của request vừa bị 401
                        // (nghĩa là request chạy trước vừa hoàn tất việc refresh), dùng ngay token mới,
                        // KHÔNG gọi API refresh lần nữa để bảo vệ token rotation.
                        if (currentRefreshToken != null &&
                            currentRefreshToken != failedRefreshToken &&
                            currentAccessToken != null
                        ) {
                            return@refreshTokens BearerTokens(
                                accessToken = currentAccessToken,
                                refreshToken = currentRefreshToken,
                            )
                        }

                        // Không có refresh token trong storage -> Hủy phiên
                        if (currentRefreshToken == null) {
                            tokenManager.clearSession()
                            return@refreshTokens null
                        }

                        // Chưa được refresh: Dùng HttpClient phụ gọi POST auth/refresh
                        try {
                            val response = refreshClient.post("auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequestDto(refreshToken = currentRefreshToken))
                            }

                            val newSession = response.body<LoginResponseDto>()
                            tokenManager.saveSession(newSession)

                            BearerTokens(
                                accessToken = newSession.accessToken,
                                refreshToken = newSession.refreshToken,
                            )
                        } catch (e: ClientRequestException) {
                            // Refresh nhận lỗi 4xx (401 hoặc AUTH_REFRESH_REUSED):
                            // Xóa toàn bộ session và phát SessionEvent.ForceLogout
                            tokenManager.clearSession()
                            null
                        } catch (e: ResponseException) {
                            if (e.response.status == HttpStatusCode.Unauthorized ||
                                e.response.status == HttpStatusCode.Forbidden
                            ) {
                                tokenManager.clearSession()
                                null
                            } else {
                                // Lỗi 5xx máy chủ: KHÔNG xóa session, ném ngoại lệ
                                throw e
                            }
                        } catch (e: HttpRequestTimeoutException) {
                            // Lỗi mạng (timeout): KHÔNG xóa session, ném ngoại lệ mạng bình thường
                            throw e
                        } catch (e: IOException) {
                            // Lỗi mạng (mất kết nối): KHÔNG xóa session, ném ngoại lệ mạng bình thường
                            throw e
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            throw e
                        }
                    }
                }

                sendWithoutRequest { request ->
                    val path = request.url.encodedPath.trimEnd('/')
                    val isPublic = path.endsWith("auth/login") ||
                            path.endsWith("auth/register") ||
                            path.endsWith("auth/refresh") ||
                            path.endsWith("auth/forgot-password") ||
                            path.endsWith("auth/reset-password") ||
                            path.endsWith("health")
                    !isPublic
                }
            }
        }
    }

    /**
     * HttpClient phụ: Dùng riêng cho việc gọi `POST auth/refresh`.
     * KHÔNG cài Auth plugin để chống lỗi lặp vô tận (infinite recursion).
     */
    fun createRefreshClient(engineFactory: HttpClientEngineFactory<*>): HttpClient {
        return HttpClient(engineFactory) {
            configureBase(this)
        }
    }

    /** Overload tạo RefreshClient với engine trực tiếp (dùng cho MockEngine trong Unit Test). */
    fun createRefreshClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            configureBase(this)
        }
    }

    /**
     * HttpClient chính: Tích hợp Ktor Auth Bearer Plugin với Mutex chống gọi song song
     * và cơ chế Double-Check Lock để đảm bảo an toàn tuyệt đối với Token Rotation.
     */
    fun createAuthenticatedClient(
        engineFactory: HttpClientEngineFactory<*>,
        tokenManager: TokenManager,
        refreshClient: HttpClient,
    ): HttpClient {
        return HttpClient(engineFactory) {
            configureBase(this)
            configureAuth(this, tokenManager, refreshClient)
        }
    }

    /** Overload tạo AuthenticatedClient với engine trực tiếp (dùng cho MockEngine trong Unit Test). */
    fun createAuthenticatedClient(
        engine: HttpClientEngine,
        tokenManager: TokenManager,
        refreshClient: HttpClient,
    ): HttpClient {
        return HttpClient(engine) {
            configureBase(this)
            configureAuth(this, tokenManager, refreshClient)
        }
    }

    /**
     * Hàm tiện ích tạo client cơ bản (chưa gắn Auth plugin), dùng cho test hoặc health check đơn giản.
     */
    fun create(engineFactory: HttpClientEngineFactory<*>): HttpClient {
        return createRefreshClient(engineFactory)
    }
}
