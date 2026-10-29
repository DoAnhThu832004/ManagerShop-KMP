// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/AuthViewModel.kt
package com.thupo.managershop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thupo.getPlatform
import com.thupo.managershop.core.AppError
import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.local.PostAuthDestination
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.domain.repository.AuthRepository
import com.thupo.managershop.domain.usecase.ValidatePasswordUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mục tiêu điều hướng sau khi thực hiện thao tác Authentication.
 */
sealed interface AuthNavTarget {
    data object Main : AuthNavTarget
    data object SetupBranch : AuthNavTarget
    data object PendingBranchAssignment : AuthNavTarget
    data class PendingApproval(val username: String) : AuthNavTarget
}

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val captchaRequired: Boolean = false,
    val captchaToken: String? = null,
    val lockCountdownSeconds: Long? = null,
    val isSuccess: Boolean = false,
)

data class RegisterUiState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isPendingApproval: Boolean = false,
    val isSuccess: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val validatePasswordUseCase: ValidatePasswordUseCase = ValidatePasswordUseCase(),
) : ViewModel() {

    private val _navEvents = MutableSharedFlow<AuthNavTarget>(replay = 0)
    val navEvents: SharedFlow<AuthNavTarget> = _navEvents.asSharedFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private var countdownJob: Job? = null

    // ── Login Handlers ─────────────────────────────────────────────────────────

    fun onLoginIdentifierChange(value: String) {
        _loginState.update { it.copy(identifier = value, errorMessage = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val currentState = _loginState.value
        if (currentState.identifier.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Vui lòng nhập email hoặc tên đăng nhập") }
            return
        }
        if (currentState.password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }

            val deviceInfo = getPlatform().name
            val result = authRepository.login(
                identifier = currentState.identifier,
                password = currentState.password,
                deviceInfo = deviceInfo,
                captchaToken = currentState.captchaToken,
            )

            when (result) {
                is Resource.Success -> {
                    _loginState.update { it.copy(isLoading = false, isSuccess = true) }
                    val dest = tokenManager.resolvePostAuthDestination()
                    val target = when (dest) {
                        PostAuthDestination.Main -> AuthNavTarget.Main
                        PostAuthDestination.SetupBranch -> AuthNavTarget.SetupBranch
                        PostAuthDestination.PendingBranchAssignment -> AuthNavTarget.PendingBranchAssignment
                        PostAuthDestination.Login -> AuthNavTarget.Main
                    }
                    _navEvents.emit(target)
                    onSuccess()
                }

                is Resource.Error -> {
                    handleLoginError(result.appError)
                }

                is Resource.Loading -> {
                    _loginState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private suspend fun handleLoginError(error: AppError) {
        when (error) {
            is AppError.InvalidCredentials -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message.ifBlank { "Tên đăng nhập hoặc mật khẩu không đúng" },
                        captchaRequired = error.captchaRequired,
                    )
                }
            }

            is AppError.AccountLocked -> {
                val seconds = if (error.retryAfterSeconds > 0) error.retryAfterSeconds else 900L
                startLockCountdown(seconds)
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Tài khoản tạm thời bị khoá. Vui lòng thử lại sau $seconds giây",
                        lockCountdownSeconds = seconds,
                    )
                }
            }

            is AppError.AccountPendingApproval -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message.ifBlank { "Tài khoản của bạn đang chờ quản trị viên phê duyệt và phân bổ chi nhánh" },
                    )
                }
                _navEvents.emit(AuthNavTarget.PendingApproval(_loginState.value.identifier.trim()))
            }

            is AppError.RateLimited -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message.ifBlank { "Bạn thao tác quá nhanh, vui lòng thử lại sau" },
                    )
                }
            }

            is AppError.Network -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không thể kết nối máy chủ, vui lòng kiểm tra kết nối mạng",
                    )
                }
            }

            is AppError.Server -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Máy chủ gặp sự cố (HTTP ${error.status}), vui lòng thử lại sau",
                    )
                }
            }

            else -> {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.toString().replace("Unknown(message=", "").trimEnd(')'),
                    )
                }
            }
        }
    }

    private fun startLockCountdown(initialSeconds: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = initialSeconds
            while (remaining > 0) {
                _loginState.update { it.copy(lockCountdownSeconds = remaining) }
                delay(1000L)
                remaining--
            }
            _loginState.update { it.copy(lockCountdownSeconds = null, errorMessage = null) }
        }
    }

    // ── Register Handlers ──────────────────────────────────────────────────────

    fun onRegisterFullNameChange(value: String) {
        _registerState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onRegisterUsernameChange(value: String) {
        _registerState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onRegisterEmailChange(value: String) {
        _registerState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onRegisterPasswordChange(value: String) {
        _registerState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        _registerState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    /**
     * Xác thực mật khẩu theo đúng chính sách bảo mật của backend (PasswordPolicyValidator).
     */
    fun validatePasswordPolicy(password: String, username: String? = null, email: String? = null): String? {
        return validatePasswordUseCase(password, username, email).errorMessage
    }

    fun register(onSuccess: () -> Unit) {
        val state = _registerState.value
        val fullName = state.fullName.trim()
        val username = state.username.trim()
        val email = state.email.trim()

        if (fullName.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Vui lòng nhập họ và tên của bạn") }
            return
        }
        if (fullName.length > 100) {
            _registerState.update { it.copy(errorMessage = "Họ và tên không được vượt quá 100 ký tự") }
            return
        }

        if (username.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Vui lòng nhập tên đăng nhập") }
            return
        }
        if (username.length < 3 || username.length > 50) {
            _registerState.update { it.copy(errorMessage = "Tên đăng nhập phải từ 3 đến 50 ký tự") }
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _registerState.update { it.copy(errorMessage = "Tên đăng nhập chỉ được chứa chữ cái, chữ số và dấu gạch dưới") }
            return
        }

        if (email.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Vui lòng nhập email") }
            return
        }
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            _registerState.update { it.copy(errorMessage = "Email không đúng định dạng") }
            return
        }
        if (email.length > 190) {
            _registerState.update { it.copy(errorMessage = "Email không được vượt quá 190 ký tự") }
            return
        }

        val policyResult = validatePasswordUseCase(state.password, username, email)
        if (!policyResult.isValid) {
            _registerState.update { it.copy(errorMessage = policyResult.errorMessage) }
            return
        }
        if (state.password != state.confirmPassword) {
            _registerState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp") }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                fullName = fullName,
                username = username,
                email = email,
                password = state.password,
                confirmPassword = state.confirmPassword,
            )

            when (result) {
                is Resource.Success -> {
                    val response = result.data
                    val isPending = response.status?.equals("PENDING_APPROVAL", ignoreCase = true) == true
                    _registerState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            isPendingApproval = isPending,
                            successMessage = response.message.ifBlank { "Đăng ký tài khoản thành công" }
                        )
                    }

                    if (isPending) {
                        _navEvents.emit(AuthNavTarget.PendingApproval(username))
                    } else {
                        // Sau khi đăng ký thành công, điền sẵn thông tin sang màn hình đăng nhập
                        _loginState.update { it.copy(identifier = username, password = state.password) }
                        onSuccess()
                    }
                }

                is Resource.Error -> {
                    val msg = when (val err = result.appError) {
                        is AppError.UserAlreadyExists -> err.message
                        is AppError.RegistrationDisabled -> err.message
                        is AppError.PasswordPolicy -> err.message
                        is AppError.RateLimited -> err.message
                        is AppError.Network -> "Không thể kết nối máy chủ, vui lòng kiểm tra kết nối mạng"
                        is AppError.Server -> "Lỗi máy chủ (HTTP ${err.status}), vui lòng thử lại sau"
                        is AppError.Unknown -> err.message.ifBlank { "Đăng ký không thành công, vui lòng thử lại" }
                        else -> "Đăng ký không thành công, vui lòng thử lại"
                    }
                    _registerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = msg,
                        )
                    }
                }

                is Resource.Loading -> {
                    _registerState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Kiểm tra nhanh trạng thái phê duyệt tài khoản (Public, không cần token).
     */
    fun checkAccountStatus(
        username: String,
        onResult: (isApproved: Boolean, message: String) -> Unit,
    ) {
        viewModelScope.launch {
            val result = authRepository.checkAccountStatus(username)
            when (result) {
                is Resource.Success -> {
                    val isApproved = result.data.status.equals("ACTIVE", ignoreCase = true)
                    onResult(isApproved, result.data.message ?: "")
                }
                is Resource.Error -> {
                    onResult(false, result.appError.message.ifBlank { "Không thể kiểm tra trạng thái lúc này" })
                }
                else -> {}
            }
        }
    }

    /**
     * Thu ngân làm mới phiên để kiểm tra xem đã được Quản lý phân bổ chi nhánh hay chưa.
     */
    fun refreshCurrentSession(
        onAssigned: (branchName: String) -> Unit,
        onStillPending: () -> Unit,
    ) {
        viewModelScope.launch {
            val result = authRepository.refreshSession()
            if (result is Resource.Success && result.data.branchId != null) {
                onAssigned(result.data.branchName ?: "Chi nhánh mới")
            } else {
                onStillPending()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
