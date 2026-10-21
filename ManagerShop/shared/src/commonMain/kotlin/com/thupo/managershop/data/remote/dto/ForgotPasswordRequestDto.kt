// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/ForgotPasswordRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)
