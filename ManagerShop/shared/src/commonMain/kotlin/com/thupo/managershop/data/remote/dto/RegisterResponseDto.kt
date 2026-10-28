// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/dto/RegisterResponseDto.kt
package com.thupo.managershop.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * DTO phản hồi thông tin sau khi đăng ký tài khoản thành công từ Backend Spring Boot:
 * {
 *   "id": 12,
 *   "username": "cashier_shop1",
 *   "email": "cashier@shop.vn",
 *   "fullName": "Nguyen Van A",
 *   "status": "ACTIVE" | "PENDING_APPROVAL",
 *   "roles": ["CASHIER"],
 *   "createdAt": "2026-10-28T03:16:46Z",
 *   "message": "Đăng ký tài khoản thành công..."
 * }
 */
@Serializable
data class RegisterResponseDto(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val status: String? = null,
    val roles: List<String> = emptyList(),
    val createdAt: Instant? = null,
    val message: String,
)
