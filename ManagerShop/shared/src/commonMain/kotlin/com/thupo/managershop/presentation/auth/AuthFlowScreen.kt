package com.thupo.managershop.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thupo.managershop.presentation.auth.components.WaveTransitionContainer

/**
 * Các phân hệ màn hình trong quy trình Authentication.
 */
enum class AuthScreenType {
    Login,
    Register
}

/**
 * Điều phối luồng Authentication với hiệu ứng chuyển màn Sóng Nước (Wave Transition).
 * Chuyển đổi mượt mà giữa [LoginScreen] và [RegisterScreen] với làn sóng thương hiệu dâng cuộn.
 */
@Composable
fun AuthFlowScreen(
    initialScreen: AuthScreenType = AuthScreenType.Login,
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onBackFromLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var targetScreen by remember { mutableStateOf(initialScreen) }

    WaveTransitionContainer(
        targetScreenKey = targetScreen,
        modifier = modifier,
        durationMs = 700,
    ) { screenKey ->
        when (screenKey as AuthScreenType) {
            AuthScreenType.Login -> {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess,
                    onNavigateToRegister = {
                        targetScreen = AuthScreenType.Register
                    },
                    onBackClick = onBackFromLogin,
                )
            }
            AuthScreenType.Register -> {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = {
                        targetScreen = AuthScreenType.Login
                    },
                    onNavigateToLogin = {
                        targetScreen = AuthScreenType.Login
                    },
                    onBackClick = {
                        targetScreen = AuthScreenType.Login
                    },
                )
            }
        }
    }
}
