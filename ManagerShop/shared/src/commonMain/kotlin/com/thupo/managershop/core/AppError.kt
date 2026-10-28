// shared/src/commonMain/kotlin/com/thupo/managershop/core/AppError.kt
package com.thupo.managershop.core

/**
 * Sealed class mô tả toàn bộ các trường hợp lỗi trong ứng dụng.
 * Map từ [com.thupo.managershop.data.remote.dto.ErrorResponseDto] qua extension [toAppError()].
 */
sealed class AppError {

    // ── Authentication / Authorization ──────────────────────────────────────

    /**
     * Sai tên đăng nhập hoặc mật khẩu.
     * Backend code: AUTH_INVALID_CREDENTIALS / AUTH_BAD_CREDENTIALS (HTTP 401)
     * @param captchaRequired Backend yêu cầu CAPTCHA ở lần đăng nhập tiếp theo.
     */
    data class InvalidCredentials(
        val captchaRequired: Boolean,
        val message: String,
    ) : AppError()

    /**
     * Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần.
     * Backend code: AUTH_ACCOUNT_LOCKED (HTTP 423)
     * @param retryAfterSeconds Thời gian chờ (giây) trước khi thử lại.
     */
    data class AccountLocked(
        val retryAfterSeconds: Long,
        val message: String,
    ) : AppError()

    /**
     * Quá nhiều yêu cầu trong thời gian ngắn (rate limiting / IP block).
     * Backend code: AUTH_RATE_LIMITED / AUTH_IP_BLOCKED (HTTP 429)
     */
    data class RateLimited(
        val retryAfterSeconds: Long,
        val message: String,
    ) : AppError()

    /**
     * Phiên đăng nhập hết hạn hoặc refresh token bị tái sử dụng.
     * Backend code: AUTH_TOKEN_EXPIRED / AUTH_REFRESH_REUSED (HTTP 401)
     */
    data class SessionExpired(val message: String) : AppError()

    /** Mật khẩu mới không đáp ứng chính sách bảo mật. */
    data class PasswordPolicy(val message: String) : AppError()

    /** Mật khẩu mới đã được dùng trước đó. */
    data class PasswordReused(val message: String) : AppError()

    /** Tên đăng nhập hoặc email đã tồn tại trong hệ thống (HTTP 409). */
    data class UserAlreadyExists(val message: String) : AppError()

    /** Tính năng đăng ký tài khoản tạm thời bị vô hiệu hoá (HTTP 403). */
    data class RegistrationDisabled(val message: String) : AppError()

    /** Tài khoản đang chờ quản trị viên phê duyệt và phân bổ chi nhánh (HTTP 403). */
    data class AccountPendingApproval(val message: String) : AppError()

    /** Không có quyền thực hiện thao tác (HTTP 403). */
    data class Forbidden(val message: String) : AppError()

    // ── Lỗi kỹ thuật ────────────────────────────────────────────────────────

    /** Không có kết nối mạng hoặc không thể kết nối tới server. */
    data class Network(val message: String) : AppError()

    /** Yêu cầu hết thời gian chờ (timeout). */
    data class Timeout(val message: String) : AppError()

    /** Không parse được JSON response từ server. */
    data class Serialization(val message: String) : AppError()

    /** Lỗi phía máy chủ (HTTP 5xx). */
    data class Server(val status: Int, val message: String) : AppError()

    /** Lỗi không xác định / chưa được xử lý. */
    data class Unknown(val message: String) : AppError()
}
