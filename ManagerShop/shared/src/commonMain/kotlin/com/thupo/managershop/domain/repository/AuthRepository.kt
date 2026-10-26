// shared/src/commonMain/kotlin/com/thupo/managershop/domain/repository/AuthRepository.kt
package com.thupo.managershop.domain.repository

import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.remote.dto.LoginResponseDto

/**
 * Interface Repository xác thực người dùng.
 * Cung cấp các phương thức đăng nhập, đăng ký và quản lý phiên.
 */
interface AuthRepository {
    /**
     * Đăng nhập hệ thống theo định danh (username hoặc email) và mật khẩu.
     * Tự động lưu Access Token, Refresh Token và thông tin phiên vào Storage an toàn khi thành công.
     */
    suspend fun login(
        identifier: String,
        password: String,
        deviceInfo: String? = null,
        captchaToken: String? = null,
    ): Resource<LoginResponseDto>

    /**
     * Đăng ký tài khoản mới cho cửa hàng hoặc nhân viên.
     */
    suspend fun register(
        shopName: String,
        identifier: String,
        password: String,
    ): Resource<Unit>
}
