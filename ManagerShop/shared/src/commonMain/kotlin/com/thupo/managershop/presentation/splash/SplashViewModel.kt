package com.thupo.managershop.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thupo.managershop.data.local.SessionStore
import com.thupo.managershop.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Các điểm đến có thể có sau khi kết thúc Splash Screen.
 */
sealed interface SplashDestination {
    /** Người dùng mở app lần đầu -> Vào màn hình giới thiệu Onboarding */
    data object Onboarding : SplashDestination

    /** Người dùng đã xem Onboarding nhưng chưa đăng nhập -> Vào màn hình Đăng nhập */
    data object Login : SplashDestination

    /** Người dùng đã đăng nhập và phiên hợp lệ -> Vào thẳng màn hình Bán hàng */
    data object Main : SplashDestination
}

/**
 * ViewModel điều phối State Machine điều hướng tại Splash Screen.
 *
 * Thực hiện đọc trạng thái ngầm bất đồng bộ song song với hoạt ảnh 2.2s của Splash,
 * đảm bảo đích đến luôn sẵn sàng với độ trễ 0ms ngay khi hoạt ảnh kết thúc.
 */
class SplashViewModel(
    private val sessionStore: SessionStore,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        checkDestination()
    }

    /**
     * Kiểm tra trạng thái điều hướng ngầm có bọc try-catch an toàn.
     */
    fun checkDestination() {
        viewModelScope.launch {
            try {
                val hasCompletedOnboarding = sessionStore.hasCompletedOnboarding()
                if (!hasCompletedOnboarding) {
                    _destination.value = SplashDestination.Onboarding
                } else if (tokenManager.hasValidSession()) {
                    _destination.value = SplashDestination.Main
                } else {
                    _destination.value = SplashDestination.Login
                }
            } catch (e: Throwable) {
                // Phòng vệ khi có lỗi đọc storage: fallback an toàn về Login
                _destination.value = SplashDestination.Login
            }
        }
    }

    /**
     * Đánh dấu người dùng đã xem xong Onboarding.
     * Chạy an toàn trên viewModelScope để không bị hủy khi màn hình Onboarding bị pop khỏi backstack.
     */
    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            try {
                sessionStore.setOnboardingCompleted(true)
            } finally {
                onCompleted()
            }
        }
    }
}
