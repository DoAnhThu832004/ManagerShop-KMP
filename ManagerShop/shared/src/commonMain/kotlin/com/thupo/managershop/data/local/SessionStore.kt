// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/SessionStore.kt
package com.thupo.managershop.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Interface lưu trữ thông tin phiên KHÔNG nhạy cảm (roles, branchId, branchName).
 * Triển khai qua DataStore Preferences Multiplatform.
 * Dữ liệu này dùng để phục hồi trạng thái UI mà không cần gọi API lại.
 */
interface SessionStore {
    /**
     * Lưu thông tin người dùng sau khi đăng nhập thành công.
     * @param roles    Danh sách vai trò (ADMIN, CASHIER, …)
     * @param branchId ID chi nhánh hiện tại (null nếu admin toàn hệ thống)
     * @param branchName Tên chi nhánh hiển thị trên UI
     */
    suspend fun saveUserData(
        roles: List<String>,
        branchId: Long?,
        branchName: String?,
    )

    /** Flow danh sách roles — emit lại mỗi khi DataStore thay đổi. */
    fun getUserRoles(): Flow<List<String>>

    /** Flow thông tin chi nhánh — emit null khi chưa đăng nhập. */
    fun getBranchInfo(): Flow<Pair<Long?, String?>?>

    /** Xóa toàn bộ thông tin phiên (đăng xuất). */
    suspend fun clearUserData()
}
