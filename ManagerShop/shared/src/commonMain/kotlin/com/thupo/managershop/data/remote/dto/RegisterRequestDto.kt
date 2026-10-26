// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/RegisterRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val fullName: String,
    val identifier: String,
    val password: String,
)
