// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/RegisterScreen.kt
package com.thupo.managershop.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.managershop.presentation.auth.components.AuthErrorBanner
import com.thupo.managershop.presentation.auth.components.ShopBackground
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPillTextField
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopPurple
import com.thupo.managershop.presentation.auth.components.ShopTopBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.registerState.collectAsState()

    RegisterContent(
        state = state,
        onFullNameChange = viewModel::onRegisterFullNameChange,
        onUsernameChange = viewModel::onRegisterUsernameChange,
        onEmailChange = viewModel::onRegisterEmailChange,
        onPasswordChange = viewModel::onRegisterPasswordChange,
        onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
        onRegisterClick = { viewModel.register(onRegisterSuccess) },
        onNavigateToLogin = onNavigateToLogin,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isInteractionEnabled = !state.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ShopTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RegisterFormHeader()
            Spacer(modifier = Modifier.height(16.dp))

            // Thông báo lỗi nếu có
            AnimatedVisibility(
                visible = !state.errorMessage.isNullOrBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    state.errorMessage?.let { message ->
                        AuthErrorBanner(errorMessage = message)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            // Thông báo tài khoản đang chờ phê duyệt
            AnimatedVisibility(
                visible = state.isPendingApproval,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "⏳ Đăng ký thành công!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.successMessage ?: "Tài khoản của bạn đang chờ Quản trị viên duyệt và phân bổ chi nhánh. Bạn có thể đăng nhập sau khi hoàn tất phê duyệt.",
                            fontSize = 13.sp,
                            color = Color(0xFF78350F),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ShopPrimaryButton(
                            text = "Đến màn hình Đăng nhập",
                            onClick = onNavigateToLogin,
                        )
                    }
                }
            }

            if (!state.isPendingApproval) {
                // Trường Họ và tên
                ShopPillTextField(
                    label = "Họ và tên",
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    placeholder = "Ví dụ: Nguyễn Văn A",
                    enabled = isInteractionEnabled,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Trường Tên đăng nhập (username)
                ShopPillTextField(
                    label = "Tên đăng nhập",
                    value = state.username,
                    onValueChange = onUsernameChange,
                    placeholder = "Ví dụ: nguyenvana (3-50 ký tự)",
                    enabled = isInteractionEnabled,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Trường Email
                ShopPillTextField(
                    label = "Email",
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "Ví dụ: vana@example.com",
                    enabled = isInteractionEnabled,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Trường Mật khẩu bảo vệ
                ShopPillTextField(
                    label = "Mật khẩu bảo vệ",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Tối thiểu 8 ký tự, có chữ hoa, thường & số",
                    isPassword = true,
                    enabled = isInteractionEnabled,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Trường Xác nhận mật khẩu
                ShopPillTextField(
                    label = "Xác nhận mật khẩu",
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "Nhập lại mật khẩu bảo vệ",
                    isPassword = true,
                    enabled = isInteractionEnabled,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (isInteractionEnabled) {
                            onRegisterClick()
                        }
                    },
                )
                Spacer(modifier = Modifier.height(18.dp))

                ShopPrimaryButton(
                    text = "Tạo tài khoản & Đăng nhập",
                    onClick = {
                        focusManager.clearFocus()
                        onRegisterClick()
                    },
                    isLoading = state.isLoading,
                    enabled = isInteractionEnabled,
                )
                Spacer(modifier = Modifier.height(12.dp))

                RegisterTermsText()
            }
        }
        RegisterFooter(onNavigateToLogin = onNavigateToLogin)
    }
}

@Composable
fun RegisterFormHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        ShopBrandLogo()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Đăng ký tài khoản",
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            letterSpacing = (-0.5).sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Khởi tạo tài khoản hệ thống ManagerShop",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B),
        )
    }
}

@Composable
fun RegisterTermsText(modifier: Modifier = Modifier) {
    Text(
        text = "Bằng việc đăng ký, bạn đồng ý tuân thủ Điều khoản sử dụng của nền tảng ManagerShop Cloud POS.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = modifier.padding(horizontal = 8.dp),
    )
}

@Composable
fun RegisterFooter(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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

@Preview
@Composable
private fun RegisterContentPreview() {
    RegisterContent(
        state = RegisterUiState(
            fullName = "Nguyễn Văn A",
            username = "nguyenvana",
            email = "vana@example.com",
        ),
        onFullNameChange = {},
        onUsernameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onNavigateToLogin = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun RegisterContentErrorPreview() {
    RegisterContent(
        state = RegisterUiState(
            fullName = "Nguyễn Văn A",
            username = "nguyenvana",
            email = "vana@example.com",
            errorMessage = "Tên đăng nhập hoặc email đã tồn tại trong hệ thống",
        ),
        onFullNameChange = {},
        onUsernameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onNavigateToLogin = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun RegisterContentPendingApprovalPreview() {
    RegisterContent(
        state = RegisterUiState(
            fullName = "Nguyễn Văn A",
            username = "nguyenvana",
            email = "vana@example.com",
            isPendingApproval = true,
            successMessage = "Tài khoản của bạn đang chờ Quản trị viên duyệt và phân bổ chi nhánh.",
        ),
        onFullNameChange = {},
        onUsernameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onNavigateToLogin = {},
        onBackClick = {},
    )
}
