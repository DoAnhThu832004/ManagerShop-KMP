package com.thupo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.thupo.managershop.navigation.AppNavHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}
