package com.thupo.managershop.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

// Bảng màu nhận diện thương hiệu chuẩn Shop
val ShopPurple = Color(0xFF5434EC)
val ShopPurpleLight = Color(0xFF6849FC)
val ShopPurpleDark = Color(0xFF3B1CBF)

/**
 * Stateful container cho màn hình Splash.
 * Quản lý chuỗi thời gian animation và báo hiệu khi hoàn thành.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    // 1. Chuyển động vào của chữ "thup" (Slanted Speed)
    val thupAlpha = remember { Animatable(0f) }
    val thupOffsetX = remember { Animatable(-30f) }

    // 2. Chuyển động xoay 360° đàn hồi của chữ 'o' xoắn ốc (Swirl Loop)
    val swirlScale = remember { Animatable(0.2f) }
    val swirlRotation = remember { Animatable(-360f) }
    val swirlAlpha = remember { Animatable(0f) }

    // 3. Vòng sóng năng lượng Shockwave khi chữ 'o' khóa khớp
    val shockwaveScale = remember { Animatable(0.4f) }
    val shockwaveAlpha = remember { Animatable(0f) }

    // 4. Tia sáng lấp lánh (Sparkle Star ✨)
    val sparkleScale = remember { Animatable(0f) }
    val sparkleRotation = remember { Animatable(0f) }
    val sparkleAlpha = remember { Animatable(0f) }

    // Tiến trình chuyển động (Chạy tuần tự đồng bộ)
    LaunchedEffect(Unit) {
        // Giai đoạn 1: Chữ "thup" nghiêng trượt vào
        launch {
            thupAlpha.animateTo(1f, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing))
        }
        launch {
            thupOffsetX.animateTo(0f, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing))
        }

        // Giai đoạn 2: Chữ 'o' xoắn ốc cuộn từ tâm xoay 360 độ và phóng lớn đàn hồi
        launch {
            swirlAlpha.animateTo(1f, animationSpec = tween(300))
        }
        launch {
            swirlRotation.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            swirlScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }

        // Giai đoạn 3: Ngay khi chữ 'o' đáp xuống (~600ms), kích hoạt Shockwave và Tia sáng
        delay(600L)
        launch {
            shockwaveAlpha.snapTo(0.85f)
            launch {
                shockwaveAlpha.animateTo(0f, animationSpec = tween(700, easing = LinearEasing))
            }
            shockwaveScale.animateTo(2.8f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }

        delay(150L)
        // Giai đoạn 4: Tia sáng lấp lánh lóe lên trên đỉnh chữ 'o'
        launch {
            sparkleAlpha.snapTo(1f)
            launch {
                sparkleRotation.animateTo(180f, animationSpec = tween(650, easing = FastOutSlowInEasing))
            }
            launch {
                sparkleScale.animateTo(1.3f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                sparkleScale.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                sparkleAlpha.animateTo(0f, animationSpec = tween(200))
            }
        }

        // Chờ tổng thời gian hiển thị (~2.2s) rồi hoàn tất chuyển màn
        delay(1400L)
        onSplashFinished()
    }

    SplashContent(
        thupAlpha = thupAlpha.value,
        thupOffsetX = thupOffsetX.value,
        swirlScale = swirlScale.value,
        swirlRotation = swirlRotation.value,
        swirlAlpha = swirlAlpha.value,
        shockwaveScale = shockwaveScale.value,
        shockwaveAlpha = shockwaveAlpha.value,
        sparkleScale = sparkleScale.value,
        sparkleRotation = sparkleRotation.value,
        sparkleAlpha = sparkleAlpha.value,
        modifier = modifier,
    )
}

/**
 * Stateless UI cho màn hình Splash.
 * Nhận các thông số hoạt ảnh từ bên ngoài, dễ dàng hiển thị trong Preview mà không cần chạy effect.
 */
@Composable
fun SplashContent(
    thupAlpha: Float,
    thupOffsetX: Float,
    swirlScale: Float,
    swirlRotation: Float,
    swirlAlpha: Float,
    shockwaveScale: Float,
    shockwaveAlpha: Float,
    sparkleScale: Float,
    sparkleRotation: Float,
    sparkleAlpha: Float,
    modifier: Modifier = Modifier,
) {
    // Nền Radial Spotlight đa lớp tạo chiều sâu không gian sân khấu
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ShopPurpleLight,
                        ShopPurple,
                        ShopPurpleDark,
                    ),
                    radius = 1200f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Logo Container
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Chữ "thup" phong cách Kiểu 4: Slanted Speed (Nghiêng 12° thể thao)
            Text(
                text = "thup",
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-2).sp,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    translationX = thupOffsetX
                    alpha = thupAlpha
                },
            )

            // Khu vực chứa chữ 'o' xoắn ốc + Shockwave + Sparkle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = 1.dp)
                    .graphicsLayer {
                        // Nghiêng nhẹ chữ 'o' đồng bộ với góc nghiêng chữ thup
                        rotationZ = -10f
                    },
            ) {
                // Vòng sóng năng lượng Shockwave Ring
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(shockwaveScale)
                        .alpha(shockwaveAlpha)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape),
                )

                // Biểu tượng chữ 'o' Swirl Loop dạng 3D Ribbon chuyển sắc
                SwirlIcon(
                    modifier = Modifier
                        .size(41.dp)
                        .graphicsLayer {
                            scaleX = swirlScale
                            scaleY = swirlScale
                            rotationZ = swirlRotation
                            alpha = swirlAlpha
                        },
                )

                // Ngôi sao lấp lánh (Sparkle Star ✨) lóe sáng ở góc trên chữ 'o'
                SparkleStar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = sparkleScale
                            scaleY = sparkleScale
                            rotationZ = sparkleRotation
                            alpha = sparkleAlpha
                        },
                )
            }
        }
    }
}

/**
 * Vẽ chữ 'o' dạng dải xoắn ốc (Shop Swirl Loop) bằng Canvas Vector chuẩn xác
 */
@Composable
fun SwirlIcon(modifier: Modifier = Modifier) {
    val swirlPath = remember {
        PathParser().parsePathString(
            "M50 12C29.0132 12 12 29.0132 12 50C12 70.9868 29.0132 88 50 88C70.9868 88 88 70.9868 88 50C88 38.5 82.5 28.5 74 22L62 34C66.5 38 69.5 43.5 69.5 50C69.5 60.8 60.8 69.5 50 69.5C39.2 69.5 30.5 60.8 30.5 50C30.5 39.2 39.2 30.5 50 30.5C54.2 30.5 58 31.8 61 34L71 22C65 16 58 12 50 12Z",
        ).toPath()
    }

    // Dải màu Gradient ánh kim lụa (White sang Lilac bạc)
    val ribbonBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color(0xFFEDE9FE),
                Color(0xFFDDD6FE),
            ),
        )
    }

    Canvas(modifier = modifier) {
        val scaleX = size.width / 100f
        val scaleY = size.height / 100f
        scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(
                path = swirlPath,
                brush = ribbonBrush,
            )
        }
    }
}

/**
 * Vẽ ngôi sao 4 cánh phát sáng lấp lánh (Sparkle Star)
 */
@Composable
fun SparkleStar(modifier: Modifier = Modifier) {
    val starPath = remember {
        PathParser().parsePathString(
            "M12 0L14.59 9.41L24 12L14.59 14.59L12 24L9.41 14.59L0 12L9.41 9.41L12 0Z",
        ).toPath()
    }

    Canvas(modifier = modifier) {
        val scaleX = size.width / 24f
        val scaleY = size.height / 24f
        scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(
                path = starPath,
                brush = SolidColor(Color.White),
            )
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun SplashContentPreview() {
    SplashContent(
        thupAlpha = 1f,
        thupOffsetX = 0f,
        swirlScale = 1f,
        swirlRotation = 0f,
        swirlAlpha = 1f,
        shockwaveScale = 1f,
        shockwaveAlpha = 0.5f,
        sparkleScale = 1f,
        sparkleRotation = 45f,
        sparkleAlpha = 1f,
    )
}
