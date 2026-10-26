package com.thupo.managershop.presentation.auth

import com.thupo.managershop.core.AppError
import com.thupo.managershop.core.Resource
import com.thupo.managershop.testutil.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        viewModel = AuthViewModel(authRepository = authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Login Tests ---

    @Test
    fun onLoginIdentifierChange_updatesIdentifierAndClearsError() {
        viewModel.onLoginIdentifierChange("user@test.com")
        assertEquals("user@test.com", viewModel.loginState.value.identifier)
        assertNull(viewModel.loginState.value.errorMessage)
    }

    @Test
    fun onLoginPasswordChange_updatesPasswordAndClearsError() {
        viewModel.onLoginPasswordChange("Secret123")
        assertEquals("Secret123", viewModel.loginState.value.password)
        assertNull(viewModel.loginState.value.errorMessage)
    }

    @Test
    fun login_withBlankIdentifier_setsErrorMessageWithoutCallingRepo() = runTest(testDispatcher) {
        viewModel.onLoginIdentifierChange("")
        viewModel.onLoginPasswordChange("Secret123")

        var successCalled = false
        viewModel.login { successCalled = true }

        assertEquals("Vui lòng nhập email hoặc tên đăng nhập", viewModel.loginState.value.errorMessage)
        assertFalse(successCalled)
        assertNull(authRepository.lastLoginIdentifier)
    }

    @Test
    fun login_withBlankPassword_setsErrorMessageWithoutCallingRepo() = runTest(testDispatcher) {
        viewModel.onLoginIdentifierChange("admin@test.com")
        viewModel.onLoginPasswordChange("")

        var successCalled = false
        viewModel.login { successCalled = true }

        assertEquals("Vui lòng nhập mật khẩu", viewModel.loginState.value.errorMessage)
        assertFalse(successCalled)
        assertNull(authRepository.lastLoginIdentifier)
    }

    @Test
    fun login_success_updatesStateAndInvokesCallback() = runTest(testDispatcher) {
        viewModel.onLoginIdentifierChange("admin@test.com")
        viewModel.onLoginPasswordChange("Secret123")

        var successCalled = false
        viewModel.login { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertTrue(viewModel.loginState.value.isSuccess)
        assertFalse(viewModel.loginState.value.isLoading)
        assertNull(viewModel.loginState.value.errorMessage)
        assertEquals("admin@test.com", authRepository.lastLoginIdentifier)
    }

    @Test
    fun login_invalidCredentials_setsErrorAndCaptchaRequired() = runTest(testDispatcher) {
        authRepository.loginResult = Resource.Error(
            AppError.InvalidCredentials(
                message = "Tên đăng nhập hoặc mật khẩu không chính xác",
                captchaRequired = true,
            )
        )
        viewModel.onLoginIdentifierChange("admin@test.com")
        viewModel.onLoginPasswordChange("Wrong123")

        var successCalled = false
        viewModel.login { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertFalse(viewModel.loginState.value.isLoading)
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác", viewModel.loginState.value.errorMessage)
        assertTrue(viewModel.loginState.value.captchaRequired)
    }

    @Test
    fun login_accountLocked_setsErrorMessageAndCountdown() = runTest(testDispatcher) {
        authRepository.loginResult = Resource.Error(
            AppError.AccountLocked(
                message = "Tài khoản bị khoá",
                retryAfterSeconds = 60L,
            )
        )
        viewModel.onLoginIdentifierChange("admin@test.com")
        viewModel.onLoginPasswordChange("Secret123")

        viewModel.login {}
        testScheduler.runCurrent()

        assertTrue(viewModel.loginState.value.errorMessage?.contains("Tài khoản tạm thời bị khoá") == true)
        assertEquals(60L, viewModel.loginState.value.lockCountdownSeconds)

        // Sau khi đếm lùi hết thời gian, trạng thái khoá tự động được gỡ bỏ
        advanceUntilIdle()
        assertNull(viewModel.loginState.value.lockCountdownSeconds)
        assertNull(viewModel.loginState.value.errorMessage)
    }

    // --- Register Tests ---

    @Test
    fun onRegisterFields_updatesStateAndClearsError() {
        viewModel.onRegisterShopNameChange("My Boutique")
        viewModel.onRegisterIdentifierChange("boutique@test.com")
        viewModel.onRegisterPasswordChange("Password123")
        viewModel.onRegisterConfirmPasswordChange("Password123")

        val state = viewModel.registerState.value
        assertEquals("My Boutique", state.shopName)
        assertEquals("boutique@test.com", state.identifier)
        assertEquals("Password123", state.password)
        assertEquals("Password123", state.confirmPassword)
        assertNull(state.errorMessage)
    }

    @Test
    fun register_withBlankShopName_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterShopNameChange("   ")
        viewModel.onRegisterIdentifierChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123")
        viewModel.onRegisterConfirmPasswordChange("Password123")

        viewModel.register {}

        assertEquals("Vui lòng nhập tên cửa hàng thời trang", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterShopName)
    }

    @Test
    fun register_withWeakPassword_setsPasswordPolicyError() = runTest(testDispatcher) {
        viewModel.onRegisterShopNameChange("My Shop")
        viewModel.onRegisterIdentifierChange("user@test.com")
        viewModel.onRegisterPasswordChange("weak")
        viewModel.onRegisterConfirmPasswordChange("weak")

        viewModel.register {}

        assertEquals("Mật khẩu phải có tối thiểu 8 ký tự", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterShopName)
    }

    @Test
    fun register_withMismatchedConfirmPassword_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterShopNameChange("My Shop")
        viewModel.onRegisterIdentifierChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123")
        viewModel.onRegisterConfirmPasswordChange("DifferentPassword123")

        viewModel.register {}

        assertEquals("Mật khẩu xác nhận không khớp", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterShopName)
    }

    @Test
    fun register_success_updatesStateAndPreFillsLoginState() = runTest(testDispatcher) {
        viewModel.onRegisterShopNameChange("My Shop")
        viewModel.onRegisterIdentifierChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123")
        viewModel.onRegisterConfirmPasswordChange("Password123")

        var successCalled = false
        viewModel.register { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertTrue(viewModel.registerState.value.isSuccess)
        assertFalse(viewModel.registerState.value.isLoading)
        // Kiểm tra điền sẵn thông tin sang login state
        assertEquals("user@test.com", viewModel.loginState.value.identifier)
        assertEquals("Password123", viewModel.loginState.value.password)
    }
}
