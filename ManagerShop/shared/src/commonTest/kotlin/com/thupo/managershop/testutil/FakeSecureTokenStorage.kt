// shared/src/commonTest/kotlin/com/thupo/managershop/testutil/FakeSecureTokenStorage.kt
package com.thupo.managershop.testutil

import com.thupo.managershop.data.local.SecureTokenStorage
import com.thupo.managershop.data.local.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake in-memory implementation của [SecureTokenStorage] dùng cho Unit Test.
 */
class FakeSecureTokenStorage(
    initialAccessToken: String? = null,
    initialRefreshToken: String? = null,
) : SecureTokenStorage {

    private var accessToken: String? = initialAccessToken
    private var refreshToken: String? = initialRefreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}

/**
 * Fake in-memory implementation của [SessionStore] dùng cho Unit Test.
 */
class FakeSessionStore(
    initialRoles: List<String> = emptyList(),
    initialBranch: Pair<Long?, String?>? = null,
) : SessionStore {

    private val rolesState = MutableStateFlow(initialRoles)
    private val branchState = MutableStateFlow(initialBranch)

    override suspend fun saveUserData(roles: List<String>, branchId: Long?, branchName: String?) {
        rolesState.value = roles
        branchState.value = if (branchId != null || branchName != null) Pair(branchId, branchName) else null
    }

    override fun getUserRoles(): Flow<List<String>> = rolesState

    override fun getBranchInfo(): Flow<Pair<Long?, String?>?> = branchState

    override suspend fun clearUserData() {
        rolesState.value = emptyList()
        branchState.value = null
    }
}
