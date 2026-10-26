// shared/src/commonMain/kotlin/com/thupo/managershop/presentation/auth/components/ShopAuthComponents.kt
package com.thupo.managershop.presentation.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Bảng màu nhận diện Shop
val ShopPurple = Color(0xFF5434EC)
val ShopPurpleLight = Color(0xFF7C3AED)
val ShopPurpleDark = Color(0xFF3B1CBF)
val ShopBackground = Color.White
val ShopPillBg = Color(0xFFF1F5F9)
val ShopBorderColor = Color(0xFFCBD5E1)

/**
 * Ô nhập liệu hình viên thuốc (Pill TextField) đồng bộ 100% giữa Login và Register.
 * Chiều cao 48dp, nhãn bên trên, nền xám chống mỏi mắt và viền bo cong hoàn toàn.
 */
@Composable
fun ShopPillTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var isFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Nhãn tiêu đề bên trên, thụt lề 16dp để thẳng hàng với đường cong
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        // Khung nhập viên thuốc cao 48dp
        val borderColor = when {
            errorMessage != null -> Color(0xFFEF4444)
            isFocused -> ShopPurple
            else -> ShopBorderColor
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(ShopPillBg)
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(9999.dp),
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
                            color = Color(0xFF94A3B8),
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                        ),
                        cursorBrush = SolidColor(ShopPurple),
                        visualTransformation = if (isPassword && !isPasswordVisible) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                    )
                }

                // Nút ẩn/hiện mật khẩu nếu là trường password
                if (isPassword) {
                    Text(
                        text = if (isPasswordVisible) "Ẩn" else "Hiện",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFocused) ShopPurple else Color(0xFF64748B),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                isPasswordVisible = !isPasswordVisible
                            }
                            .padding(start = 8.dp),
                    )
                }
            }
        }

        // Báo lỗi bên dưới ô nhập
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(start = 16.dp, top = 3.dp),
                )
            }
        }
    }
}

/**
 * Nút bấm chính màu tím điện quang #5434EC dạng viên thuốc.
 */
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

/**
 * Thanh Top Bar phong cách Shop App với nút tròn đóng/lùi và tên miền.
 */
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
        // Nút lùi tròn (Circle)
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

        // Tên miền ở giữa với chấm xanh hoạt động
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = domain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
            )
        }

        // Nút tuỳ chọn/trợ giúp
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "POS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ShopPurple,
            )
        }
    }
}

/**
 * Logo khuyên tròn Gradient chuyển sắc tím - xanh đặc trưng của Shop App.
 */
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
