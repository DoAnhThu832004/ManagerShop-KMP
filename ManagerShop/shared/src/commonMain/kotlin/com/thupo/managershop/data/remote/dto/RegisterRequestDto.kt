// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/RegisterRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO yêu cầu đăng ký tài khoản người dùng mới gửi tới endpoint POST /api/v1/auth/register.
 */
@Serializable
data class RegisterRequestDto(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)
