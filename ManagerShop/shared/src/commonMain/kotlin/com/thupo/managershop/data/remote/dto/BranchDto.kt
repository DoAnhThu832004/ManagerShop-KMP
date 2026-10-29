package com.thupo.managershop.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request DTO gửi lên backend để khởi tạo chi nhánh gốc đầu tiên cho cửa hàng.
 */
@Serializable
data class CreateBranchRequestDto(
    val code: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
)

/**
 * Response DTO trả về sau khi tạo chi nhánh thành công.
 * Mang theo cặp token mới đã nhúng claim 'bid' (branchId) để đồng bộ với SecurityContext.
 */
@Serializable
data class SetupBranchResponseDto(
    val branchId: Long,
    val branchCode: String,
    val branchName: String,
    val address: String? = null,
    val phone: String? = null,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)

/**
 * Response DTO kiểm tra trạng thái phê duyệt tài khoản (không cần token).
 */
@Serializable
data class AccountStatusResponseDto(
    val username: String,
    val status: String,
    val message: String? = null,
)
