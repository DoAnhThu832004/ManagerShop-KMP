// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/TokenManager.kt
package com.thupo.managershop.data.local

import com.thupo.managershop.data.remote.dto.LoginResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Điểm đến sau khi xác thực thành công (Single Source of Truth cho luồng điều hướng).
 */
sealed interface PostAuthDestination {
    /** Đã có chi nhánh hợp lệ -> Vào màn hình Bán hàng */
    data object Main : PostAuthDestination

    /** Chủ shop mới (ADMIN/OWNER) chưa có chi nhánh -> Vào tạo Chi nhánh đầu tiên */
    data object SetupBranch : PostAuthDestination

    /** Thu ngân (CASHIER/STAFF) chưa có chi nhánh -> Vào màn hình chờ gán quầy */
    data object PendingBranchAssignment : PostAuthDestination

    /** Chưa đăng nhập hoặc phiên không hợp lệ -> Vào màn hình Đăng nhập */
    data object Login : PostAuthDestination
}

/**
 * Quản lý trạng thái phiên đăng nhập tập trung.
 *
 * Là single source of truth cho:
 * - [isLoggedIn]: StateFlow<Boolean> — UI (NavGraph) lắng nghe để chọn màn hình chính.
 * - [sessionEvents]: SharedFlow<SessionEvent> — phát [SessionEvent.ForceLogout] khi phiên bị thu hồi.
 *
 * Được inject qua Koin dưới dạng singleton.
 *
 * @param secureStorage Lưu trữ mã hoá access/refresh token (platform-specific).
 * @param sessionStore  Lưu trữ thông tin phiên (roles, branchId) qua DataStore.
 * @param appScope      CoroutineScope singleton của ứng dụng (SupervisorJob + IO dispatcher).
 */
class TokenManager(
    private val secureStorage: SecureTokenStorage,
    private val sessionStore: SessionStore,
    private val appScope: CoroutineScope,
) {
    // ── isLoggedIn ─────────────────────────────────────────────────────────────

    /**
     * Phản ánh trạng thái đăng nhập dựa trên roles trong DataStore.
     * Emit lại ngay khi DataStore thay đổi (đăng nhập / đăng xuất).
     * Initial value = false — sẽ được cập nhật khi Flow emit lần đầu.
     */
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ── sessionEvents ──────────────────────────────────────────────────────────

    /**
     * Phát [SessionEvent.ForceLogout] khi:
     * - Refresh token hết hạn / bị tái sử dụng (AUTH_REFRESH_REUSED)
     * - Xóa session thủ công (đăng xuất)
     *
     * replay = 0: subscriber mới KHÔNG nhận sự kiện cũ (tránh vòng lặp logout).
     */
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(replay = 0)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    init {
        // Theo dõi roles trong DataStore → cập nhật isLoggedIn
        sessionStore.getUserRoles()
            .map { roles -> roles.isNotEmpty() }
            .onEach { loggedIn -> _isLoggedIn.value = loggedIn }
            .launchIn(appScope)
    }

    // ── Write ──────────────────────────────────────────────────────────────────

    /**
     * Lưu toàn bộ thông tin phiên sau khi đăng nhập thành công.
     * Phải được gọi trên IO dispatcher (đã bọc bởi [safeApiCall] hoặc [withContext(IO)]).
     */
    suspend fun saveSession(dto: LoginResponseDto) {
        secureStorage.saveTokens(
            accessToken = dto.accessToken,
            refreshToken = dto.refreshToken,
        )
        sessionStore.saveUserData(
            roles = dto.roles,
            branchId = dto.branchId,
            branchName = dto.branchName,
        )
        // isLoggedIn tự cập nhật qua Flow của sessionStore
    }

    /**
     * Xóa toàn bộ thông tin phiên (đăng xuất chủ động HOẶC token bị thu hồi).
     * Emit [SessionEvent.ForceLogout] sau khi xóa xong.
     */
    suspend fun clearSession() {
        secureStorage.clearTokens()
        sessionStore.clearUserData()
        _sessionEvents.emit(SessionEvent.ForceLogout)
        // isLoggedIn → false tự động qua Flow của sessionStore
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    /** Lấy access token hiện tại (đã giải mã). Null nếu chưa đăng nhập. */
    suspend fun getAccessToken(): String? = secureStorage.getAccessToken()

    /** Lấy refresh token hiện tại (đã giải mã). Null nếu chưa đăng nhập. */
    suspend fun getRefreshToken(): String? = secureStorage.getRefreshToken()

    /**
     * Cập nhật access token mới sau khi refresh thành công.
     * Chỉ ghi lại access token, giữ nguyên refresh token.
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        val currentRefresh = secureStorage.getRefreshToken() ?: return
        secureStorage.saveTokens(
            accessToken = newAccessToken,
            refreshToken = currentRefresh,
        )
    }

    /**
     * Cập nhật thông tin chi nhánh mới vào DataStore.
     */
    suspend fun updateBranch(branchId: Long, branchName: String) {
        sessionStore.updateBranch(branchId, branchName)
    }

    /**
     * Lưu phiên đầy đủ bao gồm cả cặp Token và thông tin phân bổ chi nhánh.
     */
    suspend fun saveFullSession(
        accessToken: String,
        refreshToken: String,
        roles: List<String>,
        branchId: Long?,
        branchName: String?,
    ) {
        secureStorage.saveTokens(accessToken, refreshToken)
        sessionStore.saveUserData(roles, branchId, branchName)
    }

    /**
     * Xác định điểm đến sau khi xác thực thành công (Single Source of Truth cho luồng điều hướng).
     */
    suspend fun resolvePostAuthDestination(): PostAuthDestination {
        return try {
            val refreshToken = secureStorage.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return PostAuthDestination.Login

            val roles = sessionStore.getUserRoles().first()
            if (roles.isEmpty()) return PostAuthDestination.Login

            val branchInfo = sessionStore.getBranchInfo().first()
            val branchId = branchInfo?.first

            val hasAdminRole = roles.any {
                it.contains("ADMIN", ignoreCase = true) || it.contains("OWNER", ignoreCase = true)
            }

            when {
                branchId != null -> PostAuthDestination.Main
                hasAdminRole -> PostAuthDestination.SetupBranch
                else -> PostAuthDestination.PendingBranchAssignment
            }
        } catch (e: Throwable) {
            PostAuthDestination.Login
        }
    }

    /**
     * Kiểm tra xem thiết bị có phiên đăng nhập hợp lệ và sẵn sàng bán hàng hay không.
     *
     * Điều kiện an toàn chuẩn Production:
     * 1. Bọc try-catch phòng ngừa ngoại lệ giải mã phần cứng (Android Keystore / JVM Keyring).
     * 2. Refresh Token tồn tại và không rỗng.
     * 3. Danh sách Roles không rỗng.
     * 4. Nghiệp vụ POS: Tài khoản quản trị toàn quyền (ADMIN/OWNER) có thể không gắn chi nhánh cố định,
     *    nhưng nhân viên thu ngân/bán hàng (CASHIER/STAFF) bắt buộc phải có [branchId] để tránh lỗi 400 API.
     */
    suspend fun hasValidSession(): Boolean {
        return try {
            val refreshToken = secureStorage.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return false

            val roles = sessionStore.getUserRoles().first()
            if (roles.isEmpty()) return false

            val branchInfo = sessionStore.getBranchInfo().first()
            val hasAdminRole = roles.any {
                it.contains("ADMIN", ignoreCase = true) || it.contains("OWNER", ignoreCase = true)
            }
            val hasValidBranch = branchInfo?.first != null

            hasAdminRole || hasValidBranch
        } catch (e: Throwable) {
            // Khi Keystore / MasterKey bị lỗi phần cứng -> coi như phiên không hợp lệ
            false
        }
    }
}
