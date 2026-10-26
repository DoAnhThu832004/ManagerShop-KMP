package com.thupo.managershop.testutil

import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import com.thupo.managershop.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var loginResult: Resource<LoginResponseDto> = Resource.Success(
        LoginResponseDto(
            accessToken = "fake_access_token",
            refreshToken = "fake_refresh_token",
            expiresIn = 3600L,
            roles = listOf("ROLE_ADMIN"),
            branchId = 1L,
            branchName = "Chi nhánh chính",
        )
    )

    var registerResult: Resource<Unit> = Resource.Success(Unit)

    var lastLoginIdentifier: String? = null
    var lastLoginPassword: String? = null
    var lastRegisterShopName: String? = null
    var lastRegisterIdentifier: String? = null
    var lastRegisterPassword: String? = null

    override suspend fun login(
        identifier: String,
        password: String,
        deviceInfo: String?,
        captchaToken: String?,
    ): Resource<LoginResponseDto> {
        lastLoginIdentifier = identifier
        lastLoginPassword = password
        return loginResult
    }

    override suspend fun register(
        shopName: String,
        identifier: String,
        password: String,
    ): Resource<Unit> {
        lastRegisterShopName = shopName
        lastRegisterIdentifier = identifier
        lastRegisterPassword = password
        return registerResult
    }
}
