package com.thupo.managershop.presentation.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import managershop.shared.generated.resources.Res
import managershop.shared.generated.resources.ic_eye
import managershop.shared.generated.resources.ic_uneye
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

val ShopPurple = Color(0xFF5434EC)
val ShopPurpleLight = Color(0xFF7C3AED)
val ShopPurpleDark = Color(0xFF3B1CBF)
val ShopBackground = Color.White
val ShopPillBg = Color(0xFFF1F5F9)
val ShopBorderColor = Color(0xFFCBD5E1)
private val ErrorColor = Color(0xFFEF4444)
private val PillShape = RoundedCornerShape(50)
private val LabelColor = Color(0xFF334155)
private val InputTextColor = Color(0xFF0F172A)
private val PlaceholderColor = Color(0xFF94A3B8)
private val ToggleNormalColor = Color(0xFF64748B)

@Composable
fun ShopPillTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = !isPassword && keyboardType != KeyboardType.Email,
        keyboardType = keyboardType,
        imeAction = imeAction,
    ),
    keyboardActions: KeyboardActions? = null,
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var lastErrorMessage by remember { mutableStateOf(errorMessage) }
    if (errorMessage != null) {
        lastErrorMessage = errorMessage
    }

    val resolvedKeyboardActions = keyboardActions ?: remember(focusManager, onImeAction) {
        KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
                onImeAction()
            },
            onDone = {
                focusManager.clearFocus()
                onImeAction()
            },
            onGo = {
                focusManager.clearFocus()
                onImeAction()
            },
            onSearch = {
                focusManager.clearFocus()
                onImeAction()
            },
        )
    }

    val borderColor = when {
        !errorMessage.isNullOrBlank() -> ErrorColor
        isFocused -> ShopPurple
        else -> ShopBorderColor
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = LabelColor,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = InputTextColor,
            ),
            cursorBrush = SolidColor(ShopPurple),
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = resolvedKeyboardActions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(PillShape)
                        .background(ShopPillBg)
                        .border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = PillShape,
                        )
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PlaceholderColor,
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                        if (isPassword) {
                            Icon(
                                painter = if (isPasswordVisible) painterResource(Res.drawable.ic_eye) else painterResource(Res.drawable.ic_uneye),
                                contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = if (isFocused) ShopPurple else ToggleNormalColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        enabled = enabled,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        isPasswordVisible = !isPasswordVisible
                                    }
                            )
//                            Text(
//                                text = if (isPasswordVisible) "Ẩn" else "Hiện",
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = if (isFocused) ShopPurple else ToggleNormalColor,
//                                modifier = Modifier
//                                    .padding(start = 8.dp)
//                                    .clickable(
//                                        enabled = enabled,
//                                        interactionSource = remember { MutableInteractionSource() },
//                                        indication = null,
//                                    ) {
//                                        isPasswordVisible = !isPasswordVisible
//                                    }
//                                    .padding(vertical = 4.dp, horizontal = 4.dp),
//                            )
                        }
                    }
                }
            },
        )

        AnimatedVisibility(
            visible = !errorMessage.isNullOrBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = lastErrorMessage.orEmpty(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ErrorColor,
                modifier = Modifier.padding(start = 16.dp, top = 3.dp),
            )
        }
    }
}

@Composable
fun ShopPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
                elevation = if (enabled && !isLoading) 6.dp else 0.dp,
                shape = RoundedCornerShape(9999.dp),
                ambientColor = ShopPurple.copy(alpha = 0.4f),
                spotColor = ShopPurple.copy(alpha = 0.5f),
            )
            .clip(RoundedCornerShape(9999.dp))
            .background(
                if (enabled) ShopPurple else Color(0xFF94A3B8)
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.2.sp,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun ShopTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    domain: String = "managershop.app",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "←",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
            )
        }

//        // Tên miền ở giữa với chấm xanh hoạt động
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center,
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(7.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF10B981)),
//            )
//            Spacer(modifier = Modifier.width(6.dp))
//            Text(
//                text = domain,
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1E293B),
//            )
//        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ShopPurple,
            )
        }
    }
}

@Composable
fun ShopBrandLogo(modifier: Modifier = Modifier) {
    val swirlPath = remember {
        PathParser().parsePathString(
            "M50 12C29.0132 12 12 29.0132 12 50C12 70.9868 29.0132 88 50 88C70.9868 88 88 70.9868 88 50C88 38.5 82.5 28.5 74 22L62 34C66.5 38 69.5 43.5 69.5 50C69.5 60.8 60.8 69.5 50 69.5C39.2 69.5 30.5 60.8 30.5 50C30.5 39.2 39.2 30.5 50 30.5C54.2 30.5 58 31.8 61 34L71 22C65 16 58 12 50 12Z"
        ).toPath()
    }

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                ShopPurpleLight,
                ShopPurple,
                Color(0xFF2563EB),
            )
        )
    }

    Box(
        modifier = modifier.size(54.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val scaleX = size.width / 100f
            val scaleY = size.height / 100f
            scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                drawPath(
                    path = swirlPath,
                    brush = gradientBrush,
                )
            }
        }
    }
}

@Composable
fun AuthErrorBanner(
    errorMessage: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFDC2626),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@Preview
@Composable
private fun ShopTopBarPreview() {
    ShopTopBar(onBackClick = {})
}

@Preview
@Composable
private fun ShopBrandLogoPreview() {
    ShopBrandLogo()
}

@Preview
@Composable
private fun ShopPillTextFieldPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShopPillTextField(
            label = "Email hoặc Số điện thoại",
            value = "admin@managershop.com",
            onValueChange = {},
            placeholder = "Nhập email",
        )
    }
}

@Preview
@Composable
private fun ShopPillTextFieldErrorPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShopPillTextField(
            label = "Mật khẩu",
            value = "123",
            onValueChange = {},
            placeholder = "Nhập mật khẩu",
            isPassword = true,
            errorMessage = "Mật khẩu phải có tối thiểu 8 ký tự",
        )
    }
}

@Preview
@Composable
private fun ShopPrimaryButtonPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShopPrimaryButton(
            text = "Tiếp tục",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ShopPrimaryButtonLoadingPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShopPrimaryButton(
            text = "Tiếp tục",
            onClick = {},
            isLoading = true,
        )
    }
}

@Preview
@Composable
private fun AuthErrorBannerPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        AuthErrorBanner(errorMessage = "Tên đăng nhập hoặc mật khẩu không chính xác")
    }
}
