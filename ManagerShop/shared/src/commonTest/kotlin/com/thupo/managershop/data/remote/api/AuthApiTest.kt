// shared/src/commonTest/kotlin/com/thupo/managershop/data/remote/api/AuthApiTest.kt
package com.thupo.managershop.data.remote.api

import com.thupo.managershop.data.remote.dto.ChangePasswordRequestDto
import com.thupo.managershop.data.remote.dto.ForgotPasswordRequestDto
import com.thupo.managershop.data.remote.dto.LoginRequestDto
import com.thupo.managershop.data.remote.dto.RefreshTokenRequestDto
import com.thupo.managershop.data.remote.dto.ResetPasswordRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthApiTest {

    private fun createMockClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            defaultRequest {
                url("https://manager-shop.onrender.com/api/v1/")
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }
    }

    @Test
    fun `login sends POST auth login and parses LoginResponseDto`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/login", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = """
                    {
                        "accessToken": "access_token_123",
                        "refreshToken": "refresh_token_456",
                        "tokenType": "Bearer",
                        "expiresIn": 3600,
                        "roles": ["ADMIN"],
                        "branchId": 1,
                        "branchName": "Chi nhánh 1",
                        "captchaRequired": false
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val response = authApi.login(
            LoginRequestDto(
                identifier = "admin",
                password = "secret_password",
            )
        )

        assertEquals("access_token_123", response.accessToken)
        assertEquals("refresh_token_456", response.refreshToken)
        assertEquals(3600L, response.expiresIn)
        assertEquals(listOf("ADMIN"), response.roles)
        assertEquals(1L, response.branchId)
        assertFalse(response.captchaRequired)
    }

    @Test
    fun `refresh sends POST auth refresh and parses new tokens`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/refresh", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = """
                    {
                        "accessToken": "new_access_token",
                        "refreshToken": "new_refresh_token",
                        "tokenType": "Bearer",
                        "expiresIn": 3600,
                        "roles": ["CASHIER"],
                        "branchId": 2,
                        "branchName": "Chi nhánh 2",
                        "captchaRequired": false
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val response = authApi.refresh(RefreshTokenRequestDto(refreshToken = "old_refresh_token"))

        assertEquals("new_access_token", response.accessToken)
        assertEquals("new_refresh_token", response.refreshToken)
        assertEquals(listOf("CASHIER"), response.roles)
    }

    @Test
    fun `logout sends POST auth logout with refreshToken body`() = runTest {
        var called = false
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/logout", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            called = true
            respond(content = "", status = HttpStatusCode.OK)
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        authApi.logout("active_refresh_token")

        assertTrue(called)
    }

    @Test
    fun `forgotPassword sends POST auth forgot-password`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/forgot-password", request.url.encodedPath)
            respond(
                content = """{"message":"Email hướng dẫn đã được gửi"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val response = authApi.forgotPassword(ForgotPasswordRequestDto(email = "user@test.com"))

        assertEquals("Email hướng dẫn đã được gửi", response.message)
    }

    @Test
    fun `resetPassword sends POST auth reset-password`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/reset-password", request.url.encodedPath)
            respond(
                content = """{"message":"Đặt lại mật khẩu thành công"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val response = authApi.resetPassword(
            ResetPasswordRequestDto(token = "valid_token", newPassword = "new_pass_123")
        )

        assertEquals("Đặt lại mật khẩu thành công", response.message)
    }

    @Test
    fun `changePassword sends POST auth change-password`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/change-password", request.url.encodedPath)
            respond(
                content = """{"message":"Đổi mật khẩu thành công"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val response = authApi.changePassword(
            ChangePasswordRequestDto(currentPassword = "old_pass", newPassword = "new_pass")
        )

        assertEquals("Đổi mật khẩu thành công", response.message)
    }

    @Test
    fun `getSessions sends GET auth sessions and parses session list`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/auth/sessions", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            respond(
                content = """
                    [
                        {
                            "id": 1,
                            "deviceInfo": "Windows 11",
                            "ipAddress": "192.168.1.10",
                            "userAgent": "ManagerShop/1.0",
                            "createdAt": "2026-10-22T00:00:00Z",
                            "expiresAt": "2026-10-23T00:00:00Z",
                            "current": true
                        }
                    ]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        val sessions = authApi.getSessions()

        assertEquals(1, sessions.size)
        assertEquals(1L, sessions.first().id)
        assertEquals("Windows 11", sessions.first().deviceInfo)
        assertTrue(sessions.first().current)
    }

    @Test
    fun `revokeSession sends DELETE auth sessions with id`() = runTest {
        var deletedId: String? = null
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Delete, request.method)
            deletedId = request.url.encodedPath.substringAfterLast("/")
            respond(content = "", status = HttpStatusCode.NoContent)
        }

        val authApi = AuthApiImpl(createMockClient(mockEngine))
        authApi.revokeSession(99L)

        assertEquals("99", deletedId)
    }
}
