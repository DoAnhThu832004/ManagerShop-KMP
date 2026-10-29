package com.thupo.managershop.presentation.branch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPillTextField
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopTopBar

/**
 * Màn hình Khởi tạo Cửa hàng & Chi nhánh đầu tiên dành cho Chủ shop mới (OWNER / ADMIN).
 * Sau khi tạo thành công, tự động lưu Token mới mang claim 'bid' và chuyển vào Bán hàng.
 */
@Composable
fun SetupBranchScreen(
    viewModel: BranchViewModel,
    onSetupSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShopBrandLogo()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Khởi tạo Cửa hàng của bạn",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tạo chi nhánh đầu tiên để kích hoạt kho hàng, danh mục sản phẩm và mở quầy bán hàng.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card thông báo lỗi (nếu có)
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            // Trường 1: Tên cửa hàng / Chi nhánh (Bắt buộc)
            ShopPillTextField(
                label = "Tên chi nhánh / Cửa hàng *",
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Ví dụ: Boutique Thu Hằng - Cơ sở 1",
                errorMessage = if (state.name.isBlank() && state.errorMessage != null) "Vui lòng nhập tên chi nhánh" else null,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trường 2: Mã chi nhánh (Tự động in hoa & gợi ý)
            ShopPillTextField(
                label = "Mã chi nhánh *",
                value = state.code,
                onValueChange = viewModel::onCodeChange,
                placeholder = "Ví dụ: CN_01",
                errorMessage = if (state.code.isBlank() && state.errorMessage != null) "Vui lòng nhập mã chi nhánh" else null,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mã quản lý kho (chữ in hoa, số và gạch dưới)",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trường 3: Địa chỉ
            ShopPillTextField(
                label = "Địa chỉ cửa hàng",
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                placeholder = "Ví dụ: 123 Cầu Giấy, Hà Nội",
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trường 4: Số điện thoại
            ShopPillTextField(
                label = "Hotline quầy thu ngân",
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = "Ví dụ: 0987654321",
                keyboardType = KeyboardType.Phone,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nút Kích hoạt & Bắt đầu bán hàng
            ShopPrimaryButton(
                text = "Kích hoạt & Mở quầy Bán hàng",
                isLoading = state.isLoading,
                enabled = !state.isLoading,
                onClick = {
                    viewModel.setupInitialBranch(onSuccess = onSetupSuccess)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
