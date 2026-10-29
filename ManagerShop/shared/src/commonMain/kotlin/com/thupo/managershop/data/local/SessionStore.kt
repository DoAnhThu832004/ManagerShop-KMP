// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/SessionStore.kt
package com.thupo.managershop.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Interface lưu trữ thông tin phiên KHÔNG nhạy cảm (roles, branchId, branchName)
 * và cấu hình ứng dụng (cờ hoàn thành Onboarding).
 * Triển khai qua DataStore Preferences Multiplatform.
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

    /** Cập nhật thông tin chi nhánh vào DataStore sau khi tạo hoặc phân bổ quầy thành công. */
    suspend fun updateBranch(branchId: Long, branchName: String)

    /** Xóa toàn bộ thông tin phiên (đăng xuất). Giữ nguyên cấu hình Onboarding. */
    suspend fun clearUserData()

    /** Ghi nhận trạng thái hoàn thành Onboarding (chỉ hiển thị 1 lần đầu). */
    suspend fun setOnboardingCompleted(completed: Boolean = true)

    /** Flow trạng thái hoàn thành Onboarding. */
    fun isOnboardingCompleted(): Flow<Boolean>

    /** Kiểm tra nhanh xem người dùng đã hoàn tất xem Onboarding hay chưa. */
    suspend fun hasCompletedOnboarding(): Boolean
}
