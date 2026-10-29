// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/api/AuthApi.kt
package com.thupo.managershop.data.remote.api

import com.thupo.managershop.data.remote.dto.AccountStatusResponseDto
import com.thupo.managershop.data.remote.dto.ChangePasswordRequestDto
import com.thupo.managershop.data.remote.dto.ForgotPasswordRequestDto
import com.thupo.managershop.data.remote.dto.LoginRequestDto
import com.thupo.managershop.data.remote.dto.LoginResponseDto
import com.thupo.managershop.data.remote.dto.MessageResponseDto
import com.thupo.managershop.data.remote.dto.RefreshTokenRequestDto
import com.thupo.managershop.data.remote.dto.RegisterRequestDto
import com.thupo.managershop.data.remote.dto.RegisterResponseDto
import com.thupo.managershop.data.remote.dto.ResetPasswordRequestDto
import com.thupo.managershop.data.remote.dto.SessionResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Interface giao tiếp với các endpoint xác thực (Authentication & Session Management) của Backend.
 */
interface AuthApi {
    /** Đăng nhập hệ thống (POST auth/login) */
    suspend fun login(request: LoginRequestDto): LoginResponseDto

    /** Đăng ký tài khoản mới (POST auth/register) */
    suspend fun register(request: RegisterRequestDto): RegisterResponseDto

    /** Làm mới Access Token bằng Refresh Token (POST auth/refresh) */
    suspend fun refresh(request: RefreshTokenRequestDto): LoginResponseDto

    /** Đăng xuất phiên hiện tại (POST auth/logout) */
    suspend fun logout(refreshToken: String)

    /** Đăng xuất toàn bộ các phiên của tài khoản (POST auth/logout-all) */
    suspend fun logoutAll()

    /** Yêu cầu gửi email đặt lại mật khẩu (POST auth/forgot-password) */
    suspend fun forgotPassword(request: ForgotPasswordRequestDto): MessageResponseDto

    /** Đặt lại mật khẩu mới qua token (POST auth/reset-password) */
    suspend fun resetPassword(request: ResetPasswordRequestDto): MessageResponseDto

    /** Đổi mật khẩu cho phiên đã đăng nhập (POST auth/change-password) */
    suspend fun changePassword(request: ChangePasswordRequestDto): MessageResponseDto

    /** Lấy danh sách các phiên đăng nhập đang hoạt động (GET auth/sessions) */
    suspend fun getSessions(): List<SessionResponseDto>

    /** Thu hồi một phiên đăng nhập cụ thể (DELETE auth/sessions/{id}) */
    suspend fun revokeSession(sessionId: Long)

    /** Kiểm tra trạng thái phê duyệt tài khoản (GET auth/check-status?username=...) */
    suspend fun checkAccountStatus(username: String): AccountStatusResponseDto
}

/**
 * Triển khai chuẩn của [AuthApi] dùng Ktor [HttpClient].
 */
class AuthApiImpl(
    private val httpClient: HttpClient,
) : AuthApi {

    override suspend fun checkAccountStatus(username: String): AccountStatusResponseDto {
        return httpClient.get("auth/check-status") {
            url {
                parameters.append("username", username)
            }
        }.body()
    }

    override suspend fun login(request: LoginRequestDto): LoginResponseDto {
        return httpClient.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun register(request: RegisterRequestDto): RegisterResponseDto {
        return httpClient.post("auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun refresh(request: RefreshTokenRequestDto): LoginResponseDto {
        return httpClient.post("auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun logout(refreshToken: String) {
        httpClient.post("auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }
    }

    override suspend fun logoutAll() {
        httpClient.post("auth/logout-all")
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequestDto): MessageResponseDto {
        return httpClient.post("auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun resetPassword(request: ResetPasswordRequestDto): MessageResponseDto {
        return httpClient.post("auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun changePassword(request: ChangePasswordRequestDto): MessageResponseDto {
        return httpClient.post("auth/change-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getSessions(): List<SessionResponseDto> {
        return httpClient.get("auth/sessions").body()
    }

    override suspend fun revokeSession(sessionId: Long) {
        httpClient.delete("auth/sessions/$sessionId")
    }
}
