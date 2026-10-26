package com.thupo.managershop.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Bảng màu chuẩn từ ảnh thời trang Shopify
val MochaTop = Color(0xFF6F5653)
val MochaMid = Color(0xFF58433F)
val MochaDark = Color(0xFF43322E)
val ShopPurple = Color(0xFF5434EC)

data class SloganItem(
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String
)

@Composable
fun FashionOnboardingScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onDemoClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // 3 Khẩu hiệu trọng tâm theo yêu cầu của bạn
    val slogans = remember {
        listOf(
            SloganItem(
                badge = "PHÂN LOẠI THỜI TRANG",
                badgeColor = Color(0xFFFDE68A), // Vàng pastel ấm
                title = "Quản Lý Size & Màu",
                subtitle = "Kiểm soát từng biến thể áo quần"
            ),
            SloganItem(
                badge = "THU NGÂN SIÊU TỐC",
                badgeColor = Color(0xFFA7F3D0), // Xanh ngọc mint
                title = "Bán Hàng POS 1 Chạm",
                subtitle = "Quét mã vạch & in bill tích tắc"
            ),
            SloganItem(
                badge = "CHÍNH XÁC REAL-TIME",
                badgeColor = Color(0xFFDDD6FE), // Tím pastel
                title = "Báo Cáo Tồn Kho Chuẩn 100%",
                subtitle = "Khỏi lo lệch kho hay thất thoát"
            )
        )
    }
    var currentSloganIndex by remember { mutableIntStateOf(0) }

    // Tự động chuyển khẩu hiệu mỗi 3.5 giây
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            currentSloganIndex = (currentSloganIndex + 1) % slogans.size
        }
    }

    // Hiệu ứng nhấp nhô nhẹ tại chỗ (chỉ 4-6px, không trôi vào tâm)
    val floatAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        floatAnim.animateTo(
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Nền Nâu Mocha thời trang chuẩn từ ảnh Princess Polly
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MochaTop,
                        MochaMid,
                        MochaDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // 1. HEADER TRÊN CÙNG
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Hệ sinh thái thời trang ",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = "thupo POS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // 2. KHU VỰC TRỌNG TÂM: 6 VẬT PHẨM NGOẠI VI & KHẨU HIỆU TRUNG TÂM KHÔNG BỊ CHE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val shift = floatAnim.value

                // ----- 6 VẬT PHẨM THỜI TRANG Ở 6 GÓC NGOẠI VI -----

                // 1. Top-Center (12h): Móc áo
                FloatingPillTag(
                    icon = "🧥",
                    text = "Móc Áo",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (8 + shift * 0.8f).dp)
                )

                // 2. Top-Right (2h): Váy đầm
                FloatingIconCard(
                    icon = "👗",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (42 - shift * 0.9f).dp)
                )

                // 3. Top-Left (10h): Áo thun
                FloatingIconCard(
                    icon = "👕",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 8.dp, y = (42 + shift * 0.7f).dp)
                )

                // 4. Bottom-Right (4h): Quần Jeans
                FloatingIconCard(
                    icon = "👖",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-42 + shift * 0.8f).dp)
                )

                // 5. Bottom-Left (8h): Thước dây Size M/L
                FloatingPillTag(
                    icon = "📏",
                    text = "Size M/L",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 6.dp, y = (-40 - shift * 0.6f).dp)
                )

                // 6. Bottom-Center (6h): Máy in Bill
                FloatingPillTag(
                    icon = "🧾",
                    text = "Máy In Bill",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-8 - shift * 0.8f).dp)
                )

                // ----- VÙNG TRUNG TÂM BẢO VỆ CHỮ (SAFE-ZONE) -----
                Box(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val currentSlogan = slogans[currentSloganIndex]

                    AnimatedContent(
                        targetState = currentSlogan,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                                    slideInVertically(animationSpec = tween(400)) { it / 2 }) togetherWith
                                    (fadeOut(animationSpec = tween(300)) +
                                            slideOutVertically(animationSpec = tween(300)) { -it / 2 })
                        },
                        label = "SloganTransition"
                    ) { slogan ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Badge phong cách tag thời trang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(
                                        width = 1.dp,
                                        color = slogan.badgeColor.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(9999.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = slogan.badge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = slogan.badgeColor
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Tiêu đề khẩu hiệu nổi bật
                            Text(
                                text = slogan.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Phụ đề giải thích
                            Text(
                                text = slogan.subtitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. KHU VỰC BUTTON CHUẨN TỪ ẢNH SHOPIFY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Nút phụ: Viên thuốc tím "Keep going ↓" phong cách Shopify
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ShopPurple)
                        .clickable { onLoginClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Vào quầy POS ngay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "↓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // NÚT CHÍNH: THANH CAPSULE "Shop all →" VỚI NÚT TRÒN MŨI TÊN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(9999.dp)
                        )
                        .clickable(enabled = !isLoading) {
                            if (!isLoading) {
                                isLoading = true
                                coroutineScope.launch {
                                    delay(1000L)
                                    isLoading = false
                                    onLoginClick()
                                }
                            }
                        }
                        .padding(start = 22.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nhãn chữ bên trái
                        Text(
                            text = if (isLoading) "Đang kết nối POS..." else "Đăng Nhập Cửa Hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )

                        // Nút tròn mũi tên (→) bên phải
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "→",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nút hành động thứ 3: Dùng thử
                Text(
                    text = "Khám phá bản dùng thử Demo Shop",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { onDemoClick() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Thẻ icon vuông bo tròn (Áo thun, Váy đầm, Quần Jeans)
 */
@Composable
private fun FloatingIconCard(
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 22.sp
        )
    }
}

/**
 * Thẻ viên thuốc dài (Móc Áo, Size M/L, Máy In Bill)
 */
@Composable
private fun FloatingPillTag(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(9999.dp))
            .clip(RoundedCornerShape(9999.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(9999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )
        }
    }
}
