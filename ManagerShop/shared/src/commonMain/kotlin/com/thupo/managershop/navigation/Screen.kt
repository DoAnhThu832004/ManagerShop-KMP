package com.thupo.managershop.navigation

import kotlinx.serialization.Serializable

/**
 * Định nghĩa Type-Safe Navigation Routes cho Jetpack Navigation Compose.
 */
sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data object Main : Screen
}
