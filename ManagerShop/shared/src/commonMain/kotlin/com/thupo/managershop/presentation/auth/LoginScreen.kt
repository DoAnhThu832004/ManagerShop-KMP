// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/LoginScreen.kt
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thupo.managershop.presentation.auth.components.AuthErrorBanner
import com.thupo.managershop.presentation.auth.components.ShopBackground
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPillTextField
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopPurple
import com.thupo.managershop.presentation.auth.components.ShopTopBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.loginState.collectAsStateWithLifecycle()

    LoginContent(
        state = state,
        onIdentifierChange = viewModel::onLoginIdentifierChange,
        onPasswordChange = viewModel::onLoginPasswordChange,
        onLoginClick = { viewModel.login(onLoginSuccess) },
        onNavigateToRegister = onNavigateToRegister,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
@Composable
fun LoginContent(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isLocked = (state.lockCountdownSeconds ?: 0L) > 0L
    val isInteractionEnabled = !state.isLoading && !isLocked
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        //ShopTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginFormHeader()
            Spacer(modifier = Modifier.height(22.dp))
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
            ShopPillTextField(
                label = "Email hoặc Tên tài khoản",
                value = state.identifier,
                onValueChange = onIdentifierChange,
                placeholder = "Ví dụ: admin@managershop.com",
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShopPillTextField(
                label = "Mật khẩu",
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Nhập mật khẩu của bạn",
                isPassword = true,
                enabled = !state.isLoading,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (isInteractionEnabled) {
                        onLoginClick()
                    }
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopPurple,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = !state.isLoading) {
                            focusManager.clearFocus()
                            onForgotPasswordClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            val isLocked = state.lockCountdownSeconds != null && state.lockCountdownSeconds > 0
            val buttonText = if (isLocked) {
                "Thử lại sau (${state.lockCountdownSeconds}s)"
            } else {
                "Tiếp tục"
            }
            ShopPrimaryButton(
                text = buttonText,
                onClick = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                isLoading = state.isLoading,
                enabled = isInteractionEnabled,
            )

            Spacer(modifier = Modifier.height(14.dp))
            LoginTermsText()
        }
        LoginFooter(onNavigateToRegister = onNavigateToRegister)
    }
}

@Composable
fun LoginFormHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ShopBrandLogo()
        Spacer(modifier = Modifier.height(14.dp))
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
    }
}

@Composable
fun LoginTermsText(modifier: Modifier = Modifier) {
    Text(
        text = "Bằng việc tiếp tục, bạn đồng ý với Điều khoản và xác nhận hiểu rõ Chính sách bảo mật của ManagerShop.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = modifier.padding(horizontal = 8.dp),
    )
}

@Composable
fun LoginFooter(
    onNavigateToRegister: () -> Unit,
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


@Preview
@Composable
private fun LoginContentPreview() {
    LoginContent(
        state = LoginUiState(identifier = "admin@managershop.com"),
        onIdentifierChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onNavigateToRegister = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun LoginContentErrorPreview() {
    LoginContent(
        state = LoginUiState(
            identifier = "admin@managershop.com",
            errorMessage = "Tên đăng nhập hoặc mật khẩu không chính xác",
        ),
        onIdentifierChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onNavigateToRegister = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun LoginContentLoadingPreview() {
    LoginContent(
        state = LoginUiState(
            identifier = "admin@managershop.com",
            isLoading = true,
        ),
        onIdentifierChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onNavigateToRegister = {},
        onBackClick = {},
    )
}

@Preview
@Composable
private fun LoginContentLockedPreview() {
    LoginContent(
        state = LoginUiState(
            identifier = "admin@managershop.com",
            errorMessage = "Tài khoản tạm thời bị khoá. Vui lòng thử lại sau 300 giây",
            lockCountdownSeconds = 300L,
        ),
        onIdentifierChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onNavigateToRegister = {},
        onBackClick = {},
    )
}
