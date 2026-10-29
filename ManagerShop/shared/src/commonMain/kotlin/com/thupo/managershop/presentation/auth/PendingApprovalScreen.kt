package com.thupo.managershop.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopPurple
import com.thupo.managershop.presentation.auth.components.ShopTopBar
import kotlinx.coroutines.launch

/**
 * Màn hình chờ quản trị viên phê duyệt tài khoản (Tokenless State).
 * Được điều hướng tới khi đăng nhập bị chặn 403 AUTH_ACCOUNT_PENDING_APPROVAL
 * hoặc khi đăng ký thành công với status == PENDING_APPROVAL.
 */
@Composable
fun PendingApprovalScreen(
    username: String,
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isChecking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessApproved by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ShopTopBar(
                onBackClick = onNavigateToLogin,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShopBrandLogo()

            Spacer(modifier = Modifier.height(28.dp))

            // Huy hiệu biểu tượng đồng hồ cát chờ duyệt
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⏳",
                    fontSize = 38.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tài khoản đang chờ xét duyệt",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tài khoản \"$username\" đã được ghi nhận trên hệ thống ManagerShop. Quản trị viên đang tiến hành kiểm tra và phân bổ chi nhánh làm việc.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card thông tin lưu ý
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Thông tin hỗ trợ:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Thời gian duyệt thông thường từ 5 đến 15 phút.\n• Bạn có thể nhấn nút bên dưới để kiểm tra lại bất cứ lúc nào.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp,
                    )
                }
            }

            AnimatedVisibility(
                visible = statusMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = statusMessage ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSuccessApproved) Color(0xFF059669) else Color(0xFFD97706),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nút 1: Kiểm tra lại trạng thái (Gọi API check-status)
            ShopPrimaryButton(
                text = if (isChecking) "Đang kiểm tra..." else "Kiểm tra lại trạng thái",
                isLoading = isChecking,
                onClick = {
                    isChecking = true
                    statusMessage = null
                    viewModel.checkAccountStatus(username) { isApproved, message ->
                        isChecking = false
                        isSuccessApproved = isApproved
                        if (isApproved) {
                            statusMessage = "🎉 Tài khoản đã được phê duyệt thành công!"
                            scope.launch {
                                snackbarHostState.showSnackbar("Tài khoản đã được duyệt! Đang chuyển về Đăng nhập...")
                            }
                            onNavigateToLogin()
                        } else {
                            statusMessage = if (message.isNotBlank()) message else "Tài khoản vẫn đang ở trạng thái chờ duyệt. Vui lòng thử lại sau ít phút."
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nút 2: Quay lại Đăng nhập
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(9999.dp))
                    .clickable { onNavigateToLogin() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Quay lại Đăng nhập",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                )
            }
        }
    }
}
