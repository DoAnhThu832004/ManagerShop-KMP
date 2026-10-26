// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/RegisterScreen.kt
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.registerState.collectAsState()

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

        // 2. Nội dung Form Đăng ký
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Logo khuyên tròn Gradient Shop
            ShopBrandLogo()

            Spacer(modifier = Modifier.height(12.dp))

            // Tiêu đề & Phụ đề
            Text(
                text = "Đăng ký tài khoản",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Khởi tạo gian hàng thời trang của bạn",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Báo lỗi validation nếu có
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

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Trường 1: Tên cửa hàng
            ShopPillTextField(
                label = "Tên cửa hàng thời trang",
                value = state.shopName,
                onValueChange = viewModel::onRegisterShopNameChange,
                placeholder = "Ví dụ: Shop Thu Hằng Boutique",
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trường 2: Email hoặc Tên tài khoản
            ShopPillTextField(
                label = "Email hoặc Số điện thoại",
                value = state.identifier,
                onValueChange = viewModel::onRegisterIdentifierChange,
                placeholder = "admin@thuhang.vn",
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trường 3: Mật khẩu (yêu cầu chuẩn backend)
            ShopPillTextField(
                label = "Mật khẩu bảo vệ",
                value = state.password,
                onValueChange = viewModel::onRegisterPasswordChange,
                placeholder = "Tối thiểu 8 ký tự, có số và chữ hoa",
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trường 4: Xác nhận mật khẩu
            ShopPillTextField(
                label = "Xác nhận mật khẩu",
                value = state.confirmPassword,
                onValueChange = viewModel::onRegisterConfirmPasswordChange,
                placeholder = "Nhập lại mật khẩu bảo vệ",
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Nút bấm Tạo tài khoản & Đăng nhập
            ShopPrimaryButton(
                text = "Tạo tài khoản & Đăng nhập",
                onClick = { viewModel.register(onRegisterSuccess) },
                isLoading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bằng việc đăng ký, bạn đồng ý tuân thủ Điều khoản sử dụng của nền tảng ManagerShop Cloud POS.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        // 3. Chân trang chuyển sang Đăng nhập
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
                    text = "Đã có tài khoản?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng nhập",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopPurple,
                    modifier = Modifier.clickable { onNavigateToLogin() },
                )
            }
        }
    }
}
