// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/SessionEvent.kt
package com.thupo.managershop.data.local

/**
 * Sealed interface mô tả các sự kiện phiên đăng nhập quan trọng.
 * [TokenManager] phát qua [SharedFlow] để UI / Navigator lắng nghe và phản ứng.
 */
sealed interface SessionEvent {
    /**
     * Phiên đăng nhập bị buộc kết thúc:
     * - Refresh token hết hạn / bị tái sử dụng (AUTH_REFRESH_REUSED)
     * - Tài khoản bị khóa / vô hiệu hóa từ phía server
     * UI phải điều hướng ngay về màn hình Login khi nhận sự kiện này.
     */
    data object ForceLogout : SessionEvent
}
