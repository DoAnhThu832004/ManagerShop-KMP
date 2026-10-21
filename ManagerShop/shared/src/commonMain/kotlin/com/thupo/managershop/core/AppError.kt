// shared/src/commonMain/kotlin/com/thupo/managershop/core/AppError.kt
package com.thupo.managershop.core

/**
 * Sealed class mô tả toàn bộ các trường hợp lỗi có thể xảy ra trong ứng dụng.
 * Mỗi lỗi nghiệp vụ từ backend hoặc lỗi kỹ thuật đều được map về một kiểu cụ thể.
 */
sealed class AppError(open val message: String? = null) {

    // --- Lỗi xác thực (Authentication / Authorization) ---

    /** Sai tên đăng nhập hoặc mật khẩu (HTTP 401). */
    data class InvalidCredentials(override val message: String? = null) : AppError(message)

    /** Tài khoản bị khóa do đăng nhập sai nhiều lần. */
    data class AccountLocked(override val message: String? = null) : AppError(message)

    /** Quá nhiều yêu cầu trong thời gian ngắn (HTTP 429). */
    data class RateLimited(override val message: String? = null) : AppError(message)

    /** Phiên đăng nhập hết hạn, cần đăng nhập lại (HTTP 401 – token expired). */
    data class SessionExpired(override val message: String? = null) : AppError(message)

    /** Mật khẩu mới không đáp ứng chính sách bảo mật. */
    data class PasswordPolicy(override val message: String? = null) : AppError(message)

    /** Mật khẩu mới đã được dùng trước đó. */
    data class PasswordReused(override val message: String? = null) : AppError(message)

    /** Không có quyền thực hiện thao tác (HTTP 403). */
    data class Forbidden(override val message: String? = null) : AppError(message)

    // --- Lỗi kỹ thuật ---

    /** Không có kết nối mạng. */
    data class Network(override val message: String? = null) : AppError(message)

    /** Yêu cầu hết thời gian chờ. */
    data class Timeout(override val message: String? = null) : AppError(message)

    /** Lỗi phía máy chủ (HTTP 5xx). */
    data class Server(val statusCode: Int? = null, override val message: String? = null) : AppError(message)

    /** Lỗi không xác định / chưa được xử lý. */
    data class Unknown(val cause: Throwable? = null, override val message: String? = null) : AppError(message)
}
