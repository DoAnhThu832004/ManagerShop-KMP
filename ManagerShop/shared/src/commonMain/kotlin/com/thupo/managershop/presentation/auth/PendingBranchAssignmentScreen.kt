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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thupo.managershop.presentation.auth.components.ShopBrandLogo
import com.thupo.managershop.presentation.auth.components.ShopPrimaryButton
import com.thupo.managershop.presentation.auth.components.ShopTopBar
import kotlinx.coroutines.launch

/**
 * Màn hình dành cho nhân viên bán hàng / thu ngân (CASHIER/STAFF) đã đăng nhập thành công
 * nhưng chưa được Quản lý / Chủ shop phân bổ chi nhánh hoặc quầy làm việc.
 */
@Composable
fun PendingBranchAssignmentScreen(
    viewModel: AuthViewModel,
    onBranchAssigned: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isReloading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ShopTopBar(
                onBackClick = onLogout,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShopBrandLogo()

            Spacer(modifier = Modifier.height(28.dp))

            // Icon quầy bán hàng / phân quyền
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🏬",
                    fontSize = 38.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Chưa được phân bổ Chi nhánh",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tài khoản nhân viên của bạn đã hoạt động nhưng chưa được chỉ định vào Chi nhánh hay Quầy bán hàng nào. Vui lòng liên hệ Quản lý hoặc Chủ shop để được phân bổ ca làm việc.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📌 Hướng dẫn thao tác:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Báo Chủ shop/Admin thêm bạn vào danh sách nhân viên chi nhánh.\n2. Sau khi Chủ shop xác nhận, bấm nút 'Tải lại dữ liệu' bên dưới để vào quầy bán hàng ngay mà không cần đăng nhập lại.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nút 1: Tải lại dữ liệu (Refresh Session lấy branchId mới)
            ShopPrimaryButton(
                text = if (isReloading) "Đang làm mới dữ liệu..." else "Tải lại dữ liệu",
                isLoading = isReloading,
                onClick = {
                    isReloading = true
                    viewModel.refreshCurrentSession(
                        onAssigned = { branchName ->
                            isReloading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã kết nối với chi nhánh: $branchName")
                            }
                            onBranchAssigned()
                        },
                        onStillPending = {
                            isReloading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Quản trị viên vẫn chưa phân bổ chi nhánh cho bạn. Vui lòng thử lại sau.")
                            }
                        }
                    )
                },
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nút 2: Đăng xuất
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(9999.dp))
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Đăng xuất tài khoản",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDC2626),
                )
            }
        }
    }
}
