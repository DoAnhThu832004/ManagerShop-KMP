// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/LoginResponseDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val roles: List<String>,
    val branchId: Long? = null,
    val branchName: String? = null,
    val captchaRequired: Boolean = false,
)
