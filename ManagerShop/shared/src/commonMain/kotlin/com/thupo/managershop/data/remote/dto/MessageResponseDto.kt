// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/MessageResponseDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponseDto(
    val message: String,
)
