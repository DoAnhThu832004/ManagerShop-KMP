// shared/src/commonMain/kotlin/com/thupo/managershop/data/repository/AuthRepositoryImpl.kt
package com.thupo.managershop.data.repository

import com.thupo.managershop.core.AppDispatchers
import com.thupo.managershop.core.Resource
import com.thupo.managershop.core.safeApiCall
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.data.remote.api.AuthApi
import com.thupo.managershop.data.remote.dto.LoginRequestDto
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import com.thupo.managershop.data.remote.dto.RegisterRequestDto
import com.thupo.managershop.data.remote.dto.RegisterResponseDto
import com.thupo.managershop.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    override suspend fun login(
        identifier: String,
        password: String,
        deviceInfo: String?,
        captchaToken: String?,
    ): Resource<LoginResponseDto> {
        val request = LoginRequestDto(
            identifier = identifier.trim(),
            password = password,
            deviceInfo = deviceInfo,
            captchaToken = captchaToken,
        )

        val result = safeApiCall(dispatchers.io) {
            authApi.login(request)
        }

        if (result is Resource.Success) {
            // Tự động lưu Token và quyền vào Secure Storage & DataStore
            tokenManager.saveSession(result.data)
        }

        return result
    }

    override suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Resource<RegisterResponseDto> {
        val request = RegisterRequestDto(
            fullName = fullName.trim(),
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            password = password,
            confirmPassword = confirmPassword,
        )

        return safeApiCall(dispatchers.io) {
            authApi.register(request)
        }
    }
}
