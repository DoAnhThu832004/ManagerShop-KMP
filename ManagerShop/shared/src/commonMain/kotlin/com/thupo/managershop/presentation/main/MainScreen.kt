package com.thupo.managershop.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.getPlatform
import com.thupo.managershop.data.remote.api.HealthApi
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Stateful container cho màn hình chính (Main POS).
 * Chịu trách nhiệm tương tác với Koin DI [HealthApi] và Coroutines.
 */
@Composable
fun MainScreen(
    healthApi: HealthApi = koinInject(),
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var serverStatus by remember { mutableStateOf("Trạng thái máy chủ: Chưa kết nối") }
    var isLoading by remember { mutableStateOf(false) }

    MainContent(
        platformName = getPlatform().name,
        serverStatus = serverStatus,
        isLoading = isLoading,
        onWarmUpClick = {
            if (!isLoading) {
                isLoading = true
                serverStatus = "Trạng thái máy chủ: Đang kết nối tới Render..."
                coroutineScope.launch {
                    val pingMs = healthApi.warmUpServer()
                    serverStatus = if (pingMs != null) {
                        "Trạng thái máy chủ: Máy chủ Hoạt động (Phản hồi: $pingMs ms)"
                    } else {
                        "Trạng thái máy chủ: Lỗi kết nối / Server đang thức dậy"
                    }
                    isLoading = false
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * Stateless UI cho màn hình chính.
 * Không phụ thuộc vào Koin hay Network API, rất dễ dàng test và preview.
 */
@Composable
fun MainContent(
    platformName: String,
    serverStatus: String,
    isLoading: Boolean,
    onWarmUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Tiêu đề ứng dụng
        Text(
            text = "managerShop POS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Thông tin nền tảng
        Text(
            text = "Nền tảng: $platformName",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Trạng thái máy chủ
        Text(
            text = serverStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                serverStatus.contains("Hoạt động") -> MaterialTheme.colorScheme.primary
                serverStatus.contains("Lỗi") -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Button warm-up gửi request thật tới Render
        Button(
            onClick = onWarmUpClick,
            enabled = !isLoading,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Đang kết nối tới Render...")
                } else {
                    Text(text = "Kiểm tra máy chủ (Warm-up)")
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun MainContentPreview() {
    MaterialTheme {
        MainContent(
            platformName = "Desktop (JVM)",
            serverStatus = "Trạng thái máy chủ: Chưa kết nối",
            isLoading = false,
            onWarmUpClick = {},
        )
    }
}

@Preview
@Composable
private fun MainContentLoadingPreview() {
    MaterialTheme {
        MainContent(
            platformName = "Android",
            serverStatus = "Trạng thái máy chủ: Đang kết nối tới Render...",
            isLoading = true,
            onWarmUpClick = {},
        )
    }
}

@Preview
@Composable
private fun MainContentOnlinePreview() {
    MaterialTheme {
        MainContent(
            platformName = "Android",
            serverStatus = "Trạng thái máy chủ: Máy chủ Hoạt động (Phản hồi: 245 ms)",
            isLoading = false,
            onWarmUpClick = {},
        )
    }
}
