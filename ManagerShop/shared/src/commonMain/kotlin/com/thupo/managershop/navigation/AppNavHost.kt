package com.thupo.managershop.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thupo.managershop.presentation.auth.AuthFlowScreen
import com.thupo.managershop.presentation.auth.AuthScreenType
import com.thupo.managershop.presentation.auth.AuthViewModel
import com.thupo.managershop.presentation.auth.LoginScreen
import com.thupo.managershop.presentation.auth.RegisterScreen
import com.thupo.managershop.presentation.main.MainScreen
import com.thupo.managershop.presentation.onboarding.FashionOnboardingScreen
import com.thupo.managershop.presentation.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Onboarding> {
            FashionOnboardingScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login)
                },
                onDemoClick = {
                    navController.navigate(Screen.Main) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Login> {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthFlowScreen(
                initialScreen = AuthScreenType.Login,
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
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
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                },
                onBackFromLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Main> {
            MainScreen()
        }
    }
}
