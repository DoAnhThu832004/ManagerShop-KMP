package com.thupo.managershop.data.repository

import com.thupo.managershop.core.AppDispatchers
import com.thupo.managershop.core.Resource
import com.thupo.managershop.core.safeApiCall
import com.thupo.managershop.data.local.SecureTokenStorage
import com.thupo.managershop.data.local.SessionStore
import com.thupo.managershop.data.remote.api.BranchApi
import com.thupo.managershop.data.remote.dto.CreateBranchRequestDto
import com.thupo.managershop.data.remote.dto.SetupBranchResponseDto
import com.thupo.managershop.domain.repository.BranchRepository

class BranchRepositoryImpl(
    private val branchApi: BranchApi,
    private val secureStorage: SecureTokenStorage,
    private val sessionStore: SessionStore,
    private val dispatchers: AppDispatchers,
) : BranchRepository {

    override suspend fun setupInitialBranch(
        code: String,
        name: String,
        address: String?,
        phone: String?,
    ): Resource<SetupBranchResponseDto> {
        val request = CreateBranchRequestDto(
            code = code.trim().uppercase(),
            name = name.trim(),
            address = address?.trim()?.ifBlank { null },
            phone = phone?.trim()?.ifBlank { null },
        )

        val result = safeApiCall(dispatchers.io) {
            branchApi.setupInitialBranch(request)
        }

        if (result is Resource.Success) {
            val data = result.data
            // 1. Cập nhật Token mới (đã có claim 'bid') vào Secure Storage
            secureStorage.saveTokens(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
            )
            // 2. Cập nhật branchId và branchName vào DataStore
            sessionStore.updateBranch(
                branchId = data.branchId,
                branchName = data.branchName,
            )
        }

        return result
    }
}
