// shared/src/commonTest/kotlin/com/thupo/managershop/data/local/TokenManagerTest.kt
package com.thupo.managershop.data.local

import app.cash.turbine.test
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TokenManagerTest {

    // ── Fake Implementations ──────────────────────────────────────────────────

    private class FakeSecureTokenStorage : SecureTokenStorage {
        private var accessToken: String? = null
        private var refreshToken: String? = null

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

    private class FakeSessionStore : SessionStore {
        private val rolesState = MutableStateFlow<List<String>>(emptyList())
        private val branchState = MutableStateFlow<Pair<Long?, String?>?>(null)

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

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `saveSession updates tokens, user data, and isLoggedIn`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val secureStorage = FakeSecureTokenStorage()
        val sessionStore = FakeSessionStore()
        val tokenManager = TokenManager(secureStorage, sessionStore, testScope)

        assertFalse(tokenManager.isLoggedIn.value)

        val loginDto = LoginResponseDto(
            accessToken = "access_123",
            refreshToken = "refresh_456",
            tokenType = "Bearer",
            expiresIn = 3600L,
            roles = listOf("ADMIN", "CASHIER"),
            branchId = 1L,
            branchName = "Chi nhánh Quận 1",
            captchaRequired = false,
        )

        tokenManager.saveSession(loginDto)

        assertEquals("access_123", tokenManager.getAccessToken())
        assertEquals("refresh_456", tokenManager.getRefreshToken())
        assertTrue(tokenManager.isLoggedIn.value)
    }

    @Test
    fun `clearSession clears tokens, data, and emits ForceLogout event`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val secureStorage = FakeSecureTokenStorage()
        val sessionStore = FakeSessionStore()
        val tokenManager = TokenManager(secureStorage, sessionStore, testScope)

        val loginDto = LoginResponseDto(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600L,
            roles = listOf("CASHIER"),
        )
        tokenManager.saveSession(loginDto)
        assertTrue(tokenManager.isLoggedIn.value)

        tokenManager.sessionEvents.test {
            tokenManager.clearSession()

            assertEquals(SessionEvent.ForceLogout, awaitItem())
            assertNull(tokenManager.getAccessToken())
            assertNull(tokenManager.getRefreshToken())
            assertFalse(tokenManager.isLoggedIn.value)
        }
    }

    @Test
    fun `updateAccessToken preserves existing refreshToken`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val secureStorage = FakeSecureTokenStorage()
        val sessionStore = FakeSessionStore()
        val tokenManager = TokenManager(secureStorage, sessionStore, testScope)

        secureStorage.saveTokens("old_access", "permanent_refresh")

        tokenManager.updateAccessToken("new_access")

        assertEquals("new_access", tokenManager.getAccessToken())
        assertEquals("permanent_refresh", tokenManager.getRefreshToken())
    }
}
