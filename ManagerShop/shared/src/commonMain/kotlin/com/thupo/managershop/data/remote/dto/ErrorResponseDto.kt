// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/ErrorResponseDto.kt
package com.thupo.managershop.data.remote.dto

import com.thupo.managershop.core.AppError
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * DTO ánh xạ chuẩn lỗi JSON từ backend Spring Boot:
 * {
 *   "timestamp": "2026-10-15T10:20:00Z",
 *   "status": 423,
 *   "code": "AUTH_ACCOUNT_LOCKED",
 *   "message": "Tài khoản tạm thời bị khoá",
 *   "retryAfterSeconds": 900,
 *   "path": "/api/v1/auth/login",
 *   "captchaRequired": false
 * }
 */
@Serializable
data class ErrorResponseDto(
    val timestamp: Instant,
    val status: Int,
    val code: String,
    val message: String,
    val retryAfterSeconds: Long? = null,
    val path: String? = null,
    val captchaRequired: Boolean? = null,
)

/**
 * Mapper từ [ErrorResponseDto] sang [AppError].
 * Ưu tiên map theo [code], fallback theo [status] nếu code chưa được định nghĩa.
 */
fun ErrorResponseDto.toAppError(): AppError = when (code) {
    // --- Auth: Invalid credentials ---
    "AUTH_INVALID_CREDENTIALS",
    "AUTH_BAD_CREDENTIALS" ->
        AppError.InvalidCredentials(
            captchaRequired = captchaRequired ?: false,
            message = message,
        )

    // --- Auth: Account locked (423) ---
    "AUTH_ACCOUNT_LOCKED" ->
        AppError.AccountLocked(
            retryAfterSeconds = retryAfterSeconds ?: 0L,
            message = message,
        )

    // --- Auth: Rate limited / IP blocked (429) ---
    "AUTH_RATE_LIMITED",
    "AUTH_IP_BLOCKED" ->
        AppError.RateLimited(
            retryAfterSeconds = retryAfterSeconds ?: 0L,
            message = message,
        )

    // --- Auth: Token / session expired (401) ---
    "AUTH_TOKEN_EXPIRED",
    "AUTH_TOKEN_INVALID",
    "AUTH_REFRESH_REUSED",
    "AUTH_REFRESH_EXPIRED" ->
        AppError.SessionExpired(message = message)

    // --- Password policy ---
    "AUTH_PASSWORD_POLICY" ->
        AppError.PasswordPolicy(message = message)

    // --- Password reuse ---
    "AUTH_PASSWORD_REUSED" ->
        AppError.PasswordReused(message = message)

    // --- Forbidden (403) ---
    "AUTH_FORBIDDEN",
    "ACCESS_DENIED" ->
        AppError.Forbidden(message = message)

    // --- Fallback: map theo HTTP status ---
    else -> when (status) {
        401 -> AppError.SessionExpired(message = message)
        403 -> AppError.Forbidden(message = message)
        423 -> AppError.AccountLocked(
            retryAfterSeconds = retryAfterSeconds ?: 0L,
            message = message,
        )
        429 -> AppError.RateLimited(
            retryAfterSeconds = retryAfterSeconds ?: 0L,
            message = message,
        )
        in 500..599 -> AppError.Server(status = status, message = message)
        else -> AppError.Unknown(message = "[$code] $message")
    }
}
