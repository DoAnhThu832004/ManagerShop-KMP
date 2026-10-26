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
import com.thupo.managershop.presentation.main.MainScreen
import com.thupo.managershop.presentation.onboarding.FashionOnboardingScreen
import com.thupo.managershop.presentation.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        popEnterTransition = { fadeIn(animationSpec = tween(500)) },
        popExitTransition = { fadeOut(animationSpec = tween(500)) }
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
                    navController.navigate(Screen.Main) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                },
                onDemoClick = {
                    navController.navigate(Screen.Main) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Main> {
            MainScreen()
        }
    }
}
