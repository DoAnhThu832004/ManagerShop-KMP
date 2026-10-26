// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/AuthViewModel.kt
package com.thupo.managershop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thupo.getPlatform
import com.thupo.managershop.core.AppError
import com.thupo.managershop.core.Resource
import com.thupo.managershop.domain.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val shopName: String = "",
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

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

    private fun handleLoginError(error: AppError) {
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

    fun onRegisterShopNameChange(value: String) {
        _registerState.update { it.copy(shopName = value, errorMessage = null) }
    }

    fun onRegisterIdentifierChange(value: String) {
        _registerState.update { it.copy(identifier = value, errorMessage = null) }
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
    fun validatePasswordPolicy(password: String): String? {
        if (password.length < 8) return "Mật khẩu phải có tối thiểu 8 ký tự"
        if (password.length > 128) return "Mật khẩu không được vượt quá 128 ký tự"
        if (!password.any { it.isUpperCase() }) return "Mật khẩu phải chứa ít nhất 1 chữ in hoa (A-Z)"
        if (!password.any { it.isLowerCase() }) return "Mật khẩu phải chứa ít nhất 1 chữ in thường (a-z)"
        if (!password.any { it.isDigit() }) return "Mật khẩu phải chứa ít nhất 1 chữ số (0-9)"
        return null
    }

    fun register(onSuccess: () -> Unit) {
        val state = _registerState.value
        if (state.shopName.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Vui lòng nhập tên cửa hàng thời trang") }
            return
        }
        if (state.identifier.isBlank()) {
            _registerState.update { it.copy(errorMessage = "Vui lòng nhập email hoặc số điện thoại") }
            return
        }
        val policyError = validatePasswordPolicy(state.password)
        if (policyError != null) {
            _registerState.update { it.copy(errorMessage = policyError) }
            return
        }
        if (state.password != state.confirmPassword) {
            _registerState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp") }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                shopName = state.shopName,
                identifier = state.identifier,
                password = state.password,
            )

            when (result) {
                is Resource.Success -> {
                    _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                    // Sau khi đăng ký thành công, điền sẵn thông tin sang màn hình đăng nhập
                    _loginState.update { it.copy(identifier = state.identifier, password = state.password) }
                    onSuccess()
                }

                is Resource.Error -> {
                    _registerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Đăng ký không thành công, vui lòng thử lại",
                        )
                    }
                }

                is Resource.Loading -> {
                    _registerState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
