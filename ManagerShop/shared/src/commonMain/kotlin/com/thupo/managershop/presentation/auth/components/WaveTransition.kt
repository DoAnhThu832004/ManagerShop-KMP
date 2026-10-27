// Lucian UX/UI Prompt #383 · MIT License (c) 2026 Seokwoooo · https://lucianlabs.dev/uxui-prompt/#383
package com.thupo.managershop.presentation.auth.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Hiệu ứng chuyển màn hình Sóng Nước (Wave Transition) chuẩn 100% theo bản demo Prompt #383.
 * Đảm bảo luồng chạy tuần tự, tuyệt đối không bị kẹt màn hình tím và hiển thị màn hình đích mượt mà.
 */
@Composable
fun WaveTransitionContainer(
    targetScreenKey: Any,
    modifier: Modifier = Modifier,
    durationMs: Int = 1500,
    waveColors: List<Color> = listOf(ShopPurpleLight, ShopPurpleDark),
    content: @Composable (screenKey: Any) -> Unit
) {
    var activeScreenKey by remember { mutableStateOf(targetScreenKey) }
    var isTransitioning by remember { mutableStateOf(false) }

    // Tiến trình sóng: 0f -> 1f
    val waveProgress = remember { Animatable(0f) }

    // Hiệu ứng trượt nổi Spring + Fade cho nội dung
    val contentOffsetY = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(1f) }

    // Lắng nghe khi targetScreenKey thay đổi (Login <-> Register)
    LaunchedEffect(targetScreenKey) {
        if (targetScreenKey != activeScreenKey && !isTransitioning) {
            isTransitioning = true
            try {
                // 1. Màn hình cũ trượt nhẹ lên trên (-14dp) và mờ dần trong khi sóng bắt đầu dâng
                launch {
                    contentOffsetY.animateTo(
                        targetValue = -14f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    contentAlpha.animateTo(
                        targetValue = 0.2f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    )
                }

                // 2. Giai đoạn 1: Sóng dâng từ đáy lên phủ kín màn hình (0f -> 0.52f, ~360ms)
                waveProgress.snapTo(0f)
                waveProgress.animateTo(
                    targetValue = 0.52f,
                    animationSpec = tween(durationMillis = (durationMs * 0.52f).toInt(), easing = LinearEasing)
                )

                // 3. Tại thời điểm này sóng đã phủ kín màn hình -> Đổi ngay sang màn hình mới
                activeScreenKey = targetScreenKey
                contentOffsetY.snapTo(26f)
                contentAlpha.snapTo(0f)

                // 4. Màn hình mới bắt đầu trượt nổi lên với Spring Bouncy
                launch {
                    contentOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.72f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
                launch {
                    contentAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    )
                }

                // 5. Giai đoạn 2: Sóng tan biến và rút qua đỉnh (0.52f -> 1.0f, ~340ms)
                waveProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = (durationMs * 0.48f).toInt(), easing = LinearEasing)
                )
            } finally {
                // Đảm bảo 100% canvas sóng được dọn sạch và nội dung mới luôn hiển thị rõ ràng
                waveProgress.snapTo(0f)
                contentOffsetY.snapTo(0f)
                contentAlpha.snapTo(1f)
                isTransitioning = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Màn hình nội dung (Login / Register)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = contentOffsetY.value * density
                    alpha = contentAlpha.value
                }
        ) {
            content(activeScreenKey)
        }

        // 2. Lớp Canvas sóng nước phủ lên trên
        val p = waveProgress.value
        if (p > 0f && p < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val currentY: Float
                val amp: Float
                val waveAlpha: Float

                if (p <= 0.52f) {
                    // Giai đoạn 1: Sóng dâng từ đáy (h + 30dp) lên qua đỉnh (-60dp)
                    val k = p / 0.52f
                    val e = if (k < 0.5f) 2f * k * k else 1f - ((-2f * k + 2f) * (-2f * k + 2f)) / 2f
                    currentY = (h + 30.dp.toPx()) - e * (h + 90.dp.toPx())
                    amp = 28.dp.toPx() * sin(k * PI).toFloat()
                    waveAlpha = 1f
                } else {
                    // Giai đoạn 2: Sóng đã qua đỉnh, mờ dần đều để hé lộ màn hình mới
                    val k = (p - 0.52f) / 0.48f
                    currentY = -60.dp.toPx() - k * 30.dp.toPx()
                    amp = 28.dp.toPx() * (1f - k) * 0.5f
                    waveAlpha = (1f - k).coerceIn(0f, 1f)
                }

                val waveBrush = Brush.linearGradient(
                    colors = waveColors,
                    start = Offset(0f, currentY.coerceAtLeast(0f)),
                    end = Offset(w, h)
                )

                val path = Path().apply {
                    moveTo(0f, h + 30.dp.toPx())

                    // Tạo đúng ~1.25 chu kỳ sóng cuộn tròn như bản demo
                    val steps = 36
                    for (i in 0..steps) {
                        val normX = i.toFloat() / steps
                        val x = w * normX
                        val waveY = currentY +
                                sin(normX * 7.89f + p * 9f) * amp +
                                sin(normX * 17.65f - p * 6f) * (amp * 0.35f)
                        lineTo(x, waveY)
                    }

                    lineTo(w, h + 30.dp.toPx())
                    lineTo(0f, h + 30.dp.toPx())
                    close()
                }

                drawPath(path = path, brush = waveBrush, alpha = waveAlpha)
            }
        }
    }
}
