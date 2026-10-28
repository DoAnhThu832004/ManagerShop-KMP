// shared/src/commonTest/kotlin/com/thupo/managershop/core/ErrorParsingTest.kt
package com.thupo.managershop.core

import com.thupo.managershop.data.remote.dto.ErrorResponseDto
import com.thupo.managershop.data.remote.dto.toAppError
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests kiểm tra cơ chế parse JSON lỗi từ backend → [AppError].
 * Không cần mạng, không cần Ktor runtime — chỉ test logic mapper thuần Kotlin.
 */
class ErrorParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun parseError(jsonString: String): AppError =
        json.decodeFromString<ErrorResponseDto>(jsonString).toAppError()

    // ── Test cases ────────────────────────────────────────────────────────────

    /**
     * Kịch bản: Backend trả 423 AUTH_ACCOUNT_LOCKED với retryAfterSeconds = 900.
     * Kỳ vọng: [AppError.AccountLocked] với retryAfterSeconds = 900.
     */
    @Test
    fun `parse 423 AUTH_ACCOUNT_LOCKED returns AccountLocked with retryAfterSeconds 900`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:20:00Z",
              "status": 423,
              "code": "AUTH_ACCOUNT_LOCKED",
              "message": "Tài khoản tạm thời bị khoá",
              "retryAfterSeconds": 900,
              "path": "/api/v1/auth/login",
              "captchaRequired": false
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.AccountLocked>(result)
        assertEquals(900L, result.retryAfterSeconds)
        assertEquals("Tài khoản tạm thời bị khoá", result.message)
    }

    /**
     * Kịch bản: Backend trả 401 AUTH_INVALID_CREDENTIALS và captchaRequired = true.
     * Kỳ vọng: [AppError.InvalidCredentials] với captchaRequired = true.
     */
    @Test
    fun `parse 401 AUTH_INVALID_CREDENTIALS with captchaRequired true returns InvalidCredentials`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:21:00Z",
              "status": 401,
              "code": "AUTH_INVALID_CREDENTIALS",
              "message": "Sai tên đăng nhập hoặc mật khẩu",
              "retryAfterSeconds": null,
              "path": "/api/v1/auth/login",
              "captchaRequired": true
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.InvalidCredentials>(result)
        assertTrue(result.captchaRequired)
        assertEquals("Sai tên đăng nhập hoặc mật khẩu", result.message)
    }

    /**
     * Kịch bản: Backend trả 429 AUTH_RATE_LIMITED với retryAfterSeconds = 60.
     * Kỳ vọng: [AppError.RateLimited] với retryAfterSeconds = 60.
     */
    @Test
    fun `parse 429 AUTH_RATE_LIMITED returns RateLimited with retryAfterSeconds 60`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:22:00Z",
              "status": 429,
              "code": "AUTH_RATE_LIMITED",
              "message": "Quá nhiều yêu cầu, vui lòng thử lại sau",
              "retryAfterSeconds": 60,
              "path": "/api/v1/auth/login",
              "captchaRequired": false
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.RateLimited>(result)
        assertEquals(60L, result.retryAfterSeconds)
    }

    /**
     * Kịch bản: Backend trả 401 AUTH_TOKEN_EXPIRED (token hết hạn).
     * Kỳ vọng: [AppError.SessionExpired].
     */
    @Test
    fun `parse 401 AUTH_TOKEN_EXPIRED returns SessionExpired`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:23:00Z",
              "status": 401,
              "code": "AUTH_TOKEN_EXPIRED",
              "message": "Phiên đăng nhập đã hết hạn",
              "path": "/api/v1/products"
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.SessionExpired>(result)
        assertEquals("Phiên đăng nhập đã hết hạn", result.message)
    }

    /**
     * Kịch bản: Backend trả code không nhận diện được, status 500.
     * Kỳ vọng: [AppError.Server] với status = 500.
     */
    @Test
    fun `parse unknown code with status 500 falls back to Server error`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:24:00Z",
              "status": 500,
              "code": "INTERNAL_UNKNOWN",
              "message": "Lỗi máy chủ nội bộ",
              "path": "/api/v1/orders"
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.Server>(result)
        assertEquals(500, result.status)
    }

    /**
     * Kịch bản: Fallback theo status 423 khi code không nhận diện được.
     * Kỳ vọng: [AppError.AccountLocked] với retryAfterSeconds = 300.
     */
    @Test
    fun `parse unknown code with status 423 and retryAfterSeconds falls back to AccountLocked`() {
        val errorJson = """
            {
              "timestamp": "2026-10-15T10:25:00Z",
              "status": 423,
              "code": "CUSTOM_LOCK_CODE",
              "message": "Tài khoản bị khoá tạm thời",
              "retryAfterSeconds": 300
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.AccountLocked>(result)
        assertEquals(300L, result.retryAfterSeconds)
    }

    /**
     * Kịch bản: Backend trả 409 AUTH_USER_ALREADY_EXISTS.
     * Kỳ vọng: [AppError.UserAlreadyExists].
     */
    @Test
    fun `parse 409 AUTH_USER_ALREADY_EXISTS returns UserAlreadyExists`() {
        val errorJson = """
            {
              "timestamp": "2026-10-28T03:16:46Z",
              "status": 409,
              "code": "AUTH_USER_ALREADY_EXISTS",
              "message": "Tên đăng nhập hoặc email đã tồn tại trong hệ thống",
              "path": "/api/v1/auth/register"
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.UserAlreadyExists>(result)
        assertEquals("Tên đăng nhập hoặc email đã tồn tại trong hệ thống", result.message)
    }

    /**
     * Kịch bản: Backend trả 403 AUTH_ACCOUNT_PENDING_APPROVAL.
     * Kỳ vọng: [AppError.AccountPendingApproval].
     */
    @Test
    fun `parse 403 AUTH_ACCOUNT_PENDING_APPROVAL returns AccountPendingApproval`() {
        val errorJson = """
            {
              "timestamp": "2026-10-28T03:16:46Z",
              "status": 403,
              "code": "AUTH_ACCOUNT_PENDING_APPROVAL",
              "message": "Tài khoản của bạn đang chờ quản trị viên phê duyệt",
              "path": "/api/v1/auth/login"
            }
        """.trimIndent()

        val result = parseError(errorJson)

        assertIs<AppError.AccountPendingApproval>(result)
        assertEquals("Tài khoản của bạn đang chờ quản trị viên phê duyệt", result.message)
    }
}
