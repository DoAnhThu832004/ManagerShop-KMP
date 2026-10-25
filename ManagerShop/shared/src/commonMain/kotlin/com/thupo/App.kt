// shared/src/commonMain/kotlin/com/thupo/App.kt
package com.thupo

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
import androidx.compose.runtime.LaunchedEffect
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
import com.thupo.managershop.data.remote.api.HealthApi
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App(
    healthApi: HealthApi = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()
    var serverStatus by remember { mutableStateOf("Trạng thái máy chủ: Chưa kết nối") }
    var isLoading by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
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
                text = "Nền tảng: ${getPlatform().name}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trạng thái máy chủ
            Text(
                text = serverStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = if (serverStatus.contains("Hoạt động")) {
                    MaterialTheme.colorScheme.primary
                } else if (serverStatus.contains("Lỗi")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button warm-up gửi request thật tới Render
            Button(
                onClick = {
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
}
