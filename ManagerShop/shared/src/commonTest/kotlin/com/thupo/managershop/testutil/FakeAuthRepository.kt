package com.thupo.managershop.testutil

import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.remote.dto.AccountStatusResponseDto
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import com.thupo.managershop.data.remote.dto.RegisterResponseDto
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

    var registerResult: Resource<RegisterResponseDto> = Resource.Success(
        RegisterResponseDto(
            id = 1L,
            username = "testuser",
            email = "user@test.com",
            fullName = "Nguyen Van A",
            status = "ACTIVE",
            roles = listOf("CASHIER"),
            message = "Đăng ký tài khoản thành công",
        )
    )

    var lastLoginIdentifier: String? = null
    var lastLoginPassword: String? = null
    var lastRegisterFullName: String? = null
    var lastRegisterUsername: String? = null
    var lastRegisterEmail: String? = null
    var lastRegisterPassword: String? = null
    var lastRegisterConfirmPassword: String? = null

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
        fullName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Resource<RegisterResponseDto> {
        lastRegisterFullName = fullName
        lastRegisterUsername = username
        lastRegisterEmail = email
        lastRegisterPassword = password
        lastRegisterConfirmPassword = confirmPassword
        return registerResult
    }

    var checkStatusResult: Resource<AccountStatusResponseDto> = Resource.Success(
        AccountStatusResponseDto(username = "testuser", status = "ACTIVE")
    )
    var refreshSessionResult: Resource<LoginResponseDto> = Resource.Success(
        LoginResponseDto(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            expiresIn = 3600L,
            roles = listOf("CASHIER"),
            branchId = 2L,
            branchName = "Chi nhánh 2",
        )
    )

    override suspend fun checkAccountStatus(username: String): Resource<AccountStatusResponseDto> {
        return checkStatusResult
    }

    override suspend fun refreshSession(): Resource<LoginResponseDto> {
        return refreshSessionResult
    }
}
