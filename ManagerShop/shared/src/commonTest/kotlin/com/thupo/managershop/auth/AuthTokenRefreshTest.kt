// shared/src/commonTest/kotlin/com/thupo/managershop/auth/AuthTokenRefreshTest.kt
package com.thupo.managershop.auth

import app.cash.turbine.test
import com.thupo.managershop.data.local.SessionEvent
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.data.remote.HttpClientFactory
import com.thupo.managershop.data.remote.api.AuthApiImpl
import com.thupo.managershop.data.remote.dto.LoginRequestDto
import com.thupo.managershop.testutil.FakeSecureTokenStorage
import com.thupo.managershop.testutil.FakeSessionStore
import com.thupo.managershop.testutil.MockEngineBuilder
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthTokenRefreshTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    /**
     * Tạo bộ đôi (mainClient, refreshClient, tokenManager, builder) chuẩn bị cho test.
     */
    private fun createTestEnvironment(
        initialAccessToken: String? = null,
        initialRefreshToken: String? = null,
        engineBuilder: MockEngineBuilder,
    ): Pair<AuthApiImpl, TokenManager> {
        val secureStorage = FakeSecureTokenStorage(
            initialAccessToken = initialAccessToken,
            initialRefreshToken = initialRefreshToken,
        )
        val sessionStore = FakeSessionStore(
            initialRoles = if (initialAccessToken != null) listOf("ADMIN") else emptyList(),
        )
        val tokenManager = TokenManager(secureStorage, sessionStore, testScope)

        val engine = engineBuilder.build()
        val refreshClient = HttpClientFactory.createRefreshClient(engine)
        val mainClient = HttpClientFactory.createAuthenticatedClient(
            engine = engine,
            tokenManager = tokenManager,
            refreshClient = refreshClient,
        )

        val authApi = AuthApiImpl(mainClient)
        return Pair(authApi, tokenManager)
    }

    // ── Test 1: Public vs Protected Header ─────────────────────────────────────

    @Test
    fun `test 1 - public endpoint has no Authorization header while protected endpoint has Bearer header`() = runTest {
        val builder = MockEngineBuilder()
            .addHandler { request ->
                if (request.url.encodedPath.endsWith("auth/login")) {
                    respond(
                        content = """
                            {
                                "accessToken": "login_access",
                                "refreshToken": "login_refresh",
                                "tokenType": "Bearer",
                                "expiresIn": 3600,
                                "roles": ["ADMIN"]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else if (request.url.encodedPath.endsWith("auth/sessions")) {
                    respond(
                        content = "[]",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else null
            }

        val (authApi, _) = createTestEnvironment(
            initialAccessToken = "my_secret_token",
            initialRefreshToken = "my_refresh_token",
            engineBuilder = builder,
        )

        // 1. Gọi endpoint công khai: login
        authApi.login(LoginRequestDto("admin", "123456"))

        // 2. Gọi endpoint bảo vệ: sessions
        authApi.getSessions()

        val requests = builder.getRecordedRequests()
        val loginRequest = requests.first { it.url.encodedPath.endsWith("auth/login") }
        val sessionsRequest = requests.first { it.url.encodedPath.endsWith("auth/sessions") }

        // Endpoint login công khai -> KHÔNG có header Authorization
        assertNull(loginRequest.headers[HttpHeaders.Authorization])

        // Endpoint sessions bảo vệ -> BẮT BUỘC có header Authorization: Bearer
        val authHeader = sessionsRequest.headers[HttpHeaders.Authorization]
        assertNotNull(authHeader)
        assertEquals("Bearer my_secret_token", authHeader)
    }

    // ── Test 2: Single 401 Refresh ─────────────────────────────────────────────

    @Test
    fun `test 2 - single 401 automatically refreshes token and retries successfully with 200 OK`() = runTest {
        val builder = MockEngineBuilder()
            .onRefresh {
                respond(
                    content = """
                        {
                            "accessToken": "new_refreshed_access",
                            "refreshToken": "new_refreshed_refresh",
                            "tokenType": "Bearer",
                            "expiresIn": 3600,
                            "roles": ["ADMIN"]
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            .addHandler { request ->
                if (request.url.encodedPath.endsWith("auth/sessions")) {
                    val authHeader = request.headers[HttpHeaders.Authorization]
                    if (authHeader == "Bearer expired_access") {
                        // Token cũ -> Trả về 401 Unauthorized
                        respond(
                            content = """{"code":"AUTH_TOKEN_EXPIRED","message":"Token expired"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else if (authHeader == "Bearer new_refreshed_access") {
                        // Token mới sau khi refresh -> Trả về 200 OK
                        respond(
                            content = "[]",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        respond(content = "Unauthorized", status = HttpStatusCode.Unauthorized)
                    }
                } else null
            }

        val (authApi, tokenManager) = createTestEnvironment(
            initialAccessToken = "expired_access",
            initialRefreshToken = "valid_refresh",
            engineBuilder = builder,
        )

        // Gọi API: client tự động xử lý 401 -> refresh -> retry thành công
        val sessions = authApi.getSessions()

        assertEquals(0, sessions.size)
        assertEquals(1, builder.getRefreshCallCount())
        assertEquals("new_refreshed_access", tokenManager.getAccessToken())
        assertEquals("new_refreshed_refresh", tokenManager.getRefreshToken())
    }

    // ── Test 3: Race Condition 5 requests đồng thời ───────────────────────────

    @Test
    fun `test 3 - race condition 5 concurrent requests with 401 calls refresh endpoint exactly once`() = runTest {
        val builder = MockEngineBuilder()
            .onRefresh(delayMs = 50) {
                respond(
                    content = """
                        {
                            "accessToken": "rotated_access_token",
                            "refreshToken": "rotated_refresh_token",
                            "tokenType": "Bearer",
                            "expiresIn": 3600,
                            "roles": ["ADMIN"]
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            .addHandler { request ->
                if (request.url.encodedPath.endsWith("auth/sessions")) {
                    val auth = request.headers[HttpHeaders.Authorization]
                    if (auth == "Bearer rotated_access_token") {
                        respond(
                            content = "[]",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else {
                        // Token ban đầu (expired) -> trả 401
                        respond(
                            content = """{"code":"AUTH_TOKEN_EXPIRED"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                } else null
            }

        val (authApi, tokenManager) = createTestEnvironment(
            initialAccessToken = "initial_expired_access",
            initialRefreshToken = "initial_valid_refresh",
            engineBuilder = builder,
        )

        // Bắn 5 request đồng thời qua Dispatchers.Default
        val deferreds = (1..5).map {
            async(Dispatchers.Default) {
                authApi.getSessions()
            }
        }

        val results = deferreds.awaitAll()

        // Cả 5 request đều thành công nhận kết quả sau retry
        assertEquals(5, results.size)
        results.forEach { assertEquals(0, it.size) }

        // ĐẶC BIỆT: Endpoint auth/refresh chỉ được gọi ĐÚNG 1 LẦN duy nhất nhờ Mutex Double-Check Lock!
        assertEquals(1, builder.getRefreshCallCount())

        // Token trong TokenManager đã được cập nhật sang token mới
        assertEquals("rotated_access_token", tokenManager.getAccessToken())
        assertEquals("rotated_refresh_token", tokenManager.getRefreshToken())
    }

    // ── Test 4: Force Logout khi Refresh thất bại ──────────────────────────────

    @Test
    fun `test 4 - when refresh fails with 401 force logout is emitted and session cleared`() = runTest {
        val builder = MockEngineBuilder()
            .onRefresh {
                respond(
                    content = """{"code":"AUTH_REFRESH_EXPIRED","message":"Refresh token has expired"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            .addHandler { request ->
                if (request.url.encodedPath.endsWith("auth/sessions")) {
                    respond(
                        content = """{"code":"AUTH_TOKEN_EXPIRED"}""",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else null
            }

        val (authApi, tokenManager) = createTestEnvironment(
            initialAccessToken = "expired_access",
            initialRefreshToken = "dead_refresh",
            engineBuilder = builder,
        )

        // Lắng nghe sự kiện ForceLogout qua Turbine
        tokenManager.sessionEvents.test {
            try {
                authApi.getSessions()
            } catch (_: Exception) {
                // Mong đợi request ném lỗi vì không thể refresh
            }

            // Kiểm tra sự kiện ForceLogout được phát
            assertEquals(SessionEvent.ForceLogout, awaitItem())

            // Kiểm tra trạng thái đăng nhập bị hủy
            assertFalse(tokenManager.isLoggedIn.value)
            assertNull(tokenManager.getAccessToken())
            assertNull(tokenManager.getRefreshToken())
        }
    }

    // ── Test 5: Network Error không xóa Session ────────────────────────────────

    @Test
    fun `test 5 - when refresh encounters network error session is preserved`() = runTest {
        val builder = MockEngineBuilder()
            .onRefresh {
                // Mô phỏng rớt mạng / socket timeout
                throw IOException("Connection timed out to Render backend")
            }
            .addHandler { request ->
                if (request.url.encodedPath.endsWith("auth/sessions")) {
                    respond(
                        content = """{"code":"AUTH_TOKEN_EXPIRED"}""",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else null
            }

        val (authApi, tokenManager) = createTestEnvironment(
            initialAccessToken = "saved_access_token",
            initialRefreshToken = "saved_refresh_token",
            engineBuilder = builder,
        )

        var caughtException: Throwable? = null
        try {
            authApi.getSessions()
        } catch (e: Throwable) {
            caughtException = e
        }

        // Ném ngoại lệ lỗi mạng
        assertNotNull(caughtException)

        // QUAN TRỌNG: Session trong TokenManager KHÔNG được phép xóa khi gặp lỗi mạng!
        assertEquals("saved_access_token", tokenManager.getAccessToken())
        assertEquals("saved_refresh_token", tokenManager.getRefreshToken())
        assertTrue(tokenManager.isLoggedIn.value)
    }
}
