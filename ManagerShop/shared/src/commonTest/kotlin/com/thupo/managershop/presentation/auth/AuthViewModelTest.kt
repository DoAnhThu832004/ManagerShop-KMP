package com.thupo.managershop.presentation.auth

import com.thupo.managershop.core.AppError
import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.data.remote.dto.RegisterResponseDto
import com.thupo.managershop.testutil.FakeAuthRepository
import com.thupo.managershop.testutil.FakeSecureTokenStorage
import com.thupo.managershop.testutil.FakeSessionStore
import kotlinx.coroutines.CoroutineScope
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
    private lateinit var tokenManager: TokenManager
    private lateinit var sessionStore: FakeSessionStore
    private lateinit var secureStorage: FakeSecureTokenStorage
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        secureStorage = FakeSecureTokenStorage()
        sessionStore = FakeSessionStore()
        tokenManager = TokenManager(
            secureStorage = secureStorage,
            sessionStore = sessionStore,
            appScope = CoroutineScope(testDispatcher),
        )
        viewModel = AuthViewModel(
            authRepository = authRepository,
            tokenManager = tokenManager,
        )
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
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("vana@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        val state = viewModel.registerState.value
        assertEquals("Nguyen Van A", state.fullName)
        assertEquals("nguyenvana", state.username)
        assertEquals("vana@test.com", state.email)
        assertEquals("Password123@", state.password)
        assertEquals("Password123@", state.confirmPassword)
        assertNull(state.errorMessage)
    }

    @Test
    fun register_withBlankFullName_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("   ")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        viewModel.register {}

        assertEquals("Vui lòng nhập họ và tên của bạn", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterFullName)
    }

    @Test
    fun register_withInvalidUsername_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("ab") // shorter than 3 chars
        viewModel.onRegisterEmailChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        viewModel.register {}

        assertEquals("Tên đăng nhập phải từ 3 đến 50 ký tự", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterUsername)
    }

    @Test
    fun register_withInvalidEmail_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("invalid_email")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        viewModel.register {}

        assertEquals("Email không đúng định dạng", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterEmail)
    }

    @Test
    fun register_withWeakPassword_setsPasswordPolicyError() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("user@test.com")
        viewModel.onRegisterPasswordChange("weak")
        viewModel.onRegisterConfirmPasswordChange("weak")

        viewModel.register {}

        assertEquals("Mật khẩu phải có tối thiểu 8 ký tự", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterFullName)
    }

    @Test
    fun register_withMismatchedConfirmPassword_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("DifferentPassword123@")

        viewModel.register {}

        assertEquals("Mật khẩu xác nhận không khớp", viewModel.registerState.value.errorMessage)
        assertNull(authRepository.lastRegisterFullName)
    }

    @Test
    fun register_success_updatesStateAndPreFillsLoginState() = runTest(testDispatcher) {
        viewModel.onRegisterFullNameChange("Nguyen Van A")
        viewModel.onRegisterUsernameChange("nguyenvana")
        viewModel.onRegisterEmailChange("user@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        var successCalled = false
        viewModel.register { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertTrue(viewModel.registerState.value.isSuccess)
        assertFalse(viewModel.registerState.value.isLoading)
        // Kiểm tra điền sẵn thông tin sang login state
        assertEquals("nguyenvana", viewModel.loginState.value.identifier)
        assertEquals("Password123@", viewModel.loginState.value.password)
    }

    @Test
    fun register_pendingApproval_setsPendingApprovalStateWithoutCallingSuccess() = runTest(testDispatcher) {
        authRepository.registerResult = Resource.Success(
            RegisterResponseDto(
                id = 101L,
                username = "pending_user",
                email = "pending@test.com",
                fullName = "Pending User",
                status = "PENDING_APPROVAL",
                roles = listOf("CASHIER"),
                message = "Đăng ký thành công. Tài khoản đang chờ quản trị viên duyệt.",
            )
        )

        viewModel.onRegisterFullNameChange("Pending User")
        viewModel.onRegisterUsernameChange("pending_user")
        viewModel.onRegisterEmailChange("pending@test.com")
        viewModel.onRegisterPasswordChange("Password123@")
        viewModel.onRegisterConfirmPasswordChange("Password123@")

        var successCalled = false
        viewModel.register { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertTrue(viewModel.registerState.value.isPendingApproval)
        assertTrue(viewModel.registerState.value.successMessage?.contains("chờ quản trị viên duyệt") == true)
    }
}
