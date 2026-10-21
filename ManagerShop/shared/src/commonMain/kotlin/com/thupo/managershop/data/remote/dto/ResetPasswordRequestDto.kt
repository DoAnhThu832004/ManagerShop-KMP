// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/ResetPasswordRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val newPassword: String,
)
