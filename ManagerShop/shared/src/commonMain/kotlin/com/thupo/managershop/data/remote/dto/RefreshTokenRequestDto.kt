// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/RefreshTokenRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)
