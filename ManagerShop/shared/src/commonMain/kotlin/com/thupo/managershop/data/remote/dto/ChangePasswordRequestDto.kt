// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/ChangePasswordRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String,
)
