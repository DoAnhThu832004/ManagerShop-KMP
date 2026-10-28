package com.thupo.managershop.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thupo.managershop.data.local.SessionEvent
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.presentation.auth.AuthFlowScreen
import com.thupo.managershop.presentation.auth.AuthScreenType
import com.thupo.managershop.presentation.auth.AuthViewModel
import com.thupo.managershop.presentation.main.MainScreen
import com.thupo.managershop.presentation.onboarding.FashionOnboardingScreen
import com.thupo.managershop.presentation.splash.SplashDestination
import com.thupo.managershop.presentation.splash.SplashScreen
import com.thupo.managershop.presentation.splash.SplashViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // ── Lắng nghe sự kiện thu hồi phiên toàn cục (ForceLogout) ─────────────────
    val tokenManager: TokenManager = koinInject()
    LaunchedEffect(Unit) {
        tokenManager.sessionEvents.collect { event ->
            if (event is SessionEvent.ForceLogout) {
                // Xóa toàn bộ backstack và chuyển về Login với launchSingleTop
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        // ── Màn hình Splash với Two-way Barrier ─────────────────────────────────
        composable<Screen.Splash> {
            val splashViewModel: SplashViewModel = koinViewModel()
            val destination by splashViewModel.destination.collectAsState()
            var isAnimationFinished by remember { mutableStateOf(false) }

            // Chỉ điều hướng khi CẢ HAI điều kiện cùng thỏa mãn (Triệt tiêu 100% Race Condition)
            LaunchedEffect(isAnimationFinished, destination) {
                val target = destination
                if (isAnimationFinished && target != null) {
                    val targetRoute = when (target) {
                        SplashDestination.Onboarding -> Screen.Onboarding
                        SplashDestination.Main -> Screen.Main
                        SplashDestination.Login -> Screen.Login
                    }
                    navController.navigate(targetRoute) {
                        popUpTo<Screen.Splash> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            SplashScreen(
                onSplashFinished = {
                    isAnimationFinished = true
                }
            )
        }

        // ── Màn hình Onboarding ────────────────────────────────────────────────
        composable<Screen.Onboarding> {
            val splashViewModel: SplashViewModel = koinViewModel()

            FashionOnboardingScreen(
                onLoginClick = {
                    // Ghi cờ Onboarding hoàn tất trên viewModelScope rồi mới navigate
                    splashViewModel.completeOnboarding {
                        navController.navigate(Screen.Login) {
                            popUpTo<Screen.Onboarding> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onDemoClick = {
                    splashViewModel.completeOnboarding {
                        navController.navigate(Screen.Main) {
                            popUpTo<Screen.Onboarding> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // ── Màn hình Authentication (Login / Register Flow) ────────────────────
        composable<Screen.Login> {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthFlowScreen(
                initialScreen = AuthScreenType.Login,
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackFromLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Register> {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthFlowScreen(
                initialScreen = AuthScreenType.Register,
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackFromLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── Màn hình Bán hàng chính (POS Terminal) ─────────────────────────────
        composable<Screen.Main> {
            MainScreen()
        }
    }
}
