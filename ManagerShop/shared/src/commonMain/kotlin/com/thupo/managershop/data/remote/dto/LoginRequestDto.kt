// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/LoginRequestDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String,
    val deviceInfo: String? = null,
    val captchaToken: String? = null,
)
