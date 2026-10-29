package com.thupo.managershop.domain.repository

import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.remote.dto.SetupBranchResponseDto

/**
 * Interface Repository quản lý Chi nhánh.
 */
interface BranchRepository {
    /**
     * Khởi tạo chi nhánh gốc đầu tiên cho Chủ shop (Owner).
     * Tự động cập nhật Token mới có claim 'bid' và cập nhật DataStore khi thành công.
     */
    suspend fun setupInitialBranch(
        code: String,
        name: String,
        address: String? = null,
        phone: String? = null,
    ): Resource<SetupBranchResponseDto>
}
