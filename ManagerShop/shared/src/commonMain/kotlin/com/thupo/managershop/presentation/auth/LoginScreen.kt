// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/LoginScreen.kt
package com.thupo.managershop.presentation.auth

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.managershop.presentation.auth.components.ShopBackground
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPillTextField
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopPurple
import com.thupo.managershop.presentation.auth.components.ShopTopBar

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.loginState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // 1. Thanh Top Bar
        ShopTopBar(onBackClick = onBackClick)

        // 2. Nội dung Form chính
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Logo khuyên tròn Gradient Shop
            ShopBrandLogo()

            Spacer(modifier = Modifier.height(14.dp))

            // Tiêu đề & Phụ đề
            Text(
                text = "Đăng nhập vào Shop",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hoặc tạo tài khoản mới để bắt đầu",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Khung thông báo lỗi / Cảnh báo khoá tài khoản nếu có
            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Trường 1: Email hoặc Tên tài khoản
            ShopPillTextField(
                label = "Email hoặc Tên tài khoản",
                value = state.identifier,
                onValueChange = viewModel::onLoginIdentifierChange,
                placeholder = "Ví dụ: admin@managershop.com",
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trường 2: Mật khẩu
            ShopPillTextField(
                label = "Mật khẩu",
                value = state.password,
                onValueChange = viewModel::onLoginPasswordChange,
                placeholder = "Nhập mật khẩu của bạn",
                isPassword = true,
            )

            // Link Quên mật khẩu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, end = 14.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopPurple,
                    modifier = Modifier.clickable {
                        // Tính năng đặt lại mật khẩu theo ForgotPasswordRequest
                    },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nút bấm Tiếp tục
            val isLocked = state.lockCountdownSeconds != null && state.lockCountdownSeconds!! > 0
            val buttonText = if (isLocked) {
                "Thử lại sau (${state.lockCountdownSeconds}s)"
            } else {
                "Tiếp tục"
            }

            ShopPrimaryButton(
                text = buttonText,
                onClick = { viewModel.login(onLoginSuccess) },
                isLoading = state.isLoading,
                enabled = !isLocked,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Điều khoản & Chính sách bảo mật
            Text(
                text = "Bằng việc tiếp tục, bạn đồng ý với Điều khoản và xác nhận hiểu rõ Chính sách bảo mật của ManagerShop.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        // 3. Chân trang chuyển sang Đăng ký
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Chưa có tài khoản cửa hàng?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng ký ngay",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopPurple,
                    modifier = Modifier.clickable { onNavigateToRegister() },
                )
            }
        }
    }
}
