package com.thupo.managershop.presentation.splash

import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.testutil.FakeSecureTokenStorage
import com.thupo.managershop.testutil.FakeSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSessionStore: FakeSessionStore
    private lateinit var fakeSecureStorage: FakeSecureTokenStorage
    private lateinit var tokenManager: TokenManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSessionStore = FakeSessionStore()
        fakeSecureStorage = FakeSecureTokenStorage()
        tokenManager = TokenManager(
            secureStorage = fakeSecureStorage,
            sessionStore = fakeSessionStore,
            appScope = CoroutineScope(SupervisorJob() + testDispatcher),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkDestination_whenOnboardingNotCompleted_emitsOnboarding() = runTest(testDispatcher) {
        fakeSessionStore.setOnboardingCompleted(false)

        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        assertEquals(SplashDestination.Onboarding, viewModel.destination.value)
    }

    @Test
    fun checkDestination_whenOnboardingCompletedAndNoSession_emitsLogin() = runTest(testDispatcher) {
        fakeSessionStore.setOnboardingCompleted(true)
        fakeSecureStorage.clearTokens()
        fakeSessionStore.clearUserData()

        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, viewModel.destination.value)
    }

    @Test
    fun checkDestination_whenCashierHasNoBranch_emitsLogin() = runTest(testDispatcher) {
        // Kịch bản POS: Thu ngân có token nhưng chưa có branchId -> Không được vào Main
        fakeSessionStore.setOnboardingCompleted(true)
        fakeSecureStorage.saveTokens("access_token", "refresh_token")
        fakeSessionStore.saveUserData(listOf("CASHIER"), branchId = null, branchName = null)

        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, viewModel.destination.value)
    }

    @Test
    fun checkDestination_whenAdminHasNoBranch_emitsMain() = runTest(testDispatcher) {
        // Kịch bản POS: Quản trị viên (ADMIN) toàn quyền có thể không gán chi nhánh cố định
        fakeSessionStore.setOnboardingCompleted(true)
        fakeSecureStorage.saveTokens("access_token", "refresh_token")
        fakeSessionStore.saveUserData(listOf("ADMIN"), branchId = null, branchName = null)

        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        assertEquals(SplashDestination.Main, viewModel.destination.value)
    }

    @Test
    fun checkDestination_whenCashierHasValidBranch_emitsMain() = runTest(testDispatcher) {
        fakeSessionStore.setOnboardingCompleted(true)
        fakeSecureStorage.saveTokens("access_token", "refresh_token")
        fakeSessionStore.saveUserData(listOf("CASHIER"), branchId = 1L, branchName = "Chi nhánh Quận 1")

        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        assertEquals(SplashDestination.Main, viewModel.destination.value)
    }

    @Test
    fun completeOnboarding_setsOnboardingCompletedAndCallsCallback() = runTest(testDispatcher) {
        fakeSessionStore.setOnboardingCompleted(false)
        val viewModel = SplashViewModel(fakeSessionStore, tokenManager)
        advanceUntilIdle()

        var callbackCalled = false
        viewModel.completeOnboarding {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertTrue(fakeSessionStore.hasCompletedOnboarding())
    }
}
