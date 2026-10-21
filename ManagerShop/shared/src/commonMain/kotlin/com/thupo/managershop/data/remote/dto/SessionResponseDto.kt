// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/SessionResponseDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SessionResponseDto(
    val id: Long,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: Instant,
    val expiresAt: Instant,
    val current: Boolean,
)
