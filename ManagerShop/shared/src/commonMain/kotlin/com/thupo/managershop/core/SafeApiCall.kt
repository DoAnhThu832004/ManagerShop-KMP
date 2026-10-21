// shared/src/commonMain/kotlin/com/thupo/managershop/core/SafeApiCall.kt
package com.thupo.managershop.core

import com.thupo.managershop.data.remote.dto.ErrorResponseDto
import com.thupo.managershop.data.remote.dto.toAppError
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Helper bọc mọi lời gọi Ktor API, bắt tất cả exception và convert sang [Resource].
 *
 * Thứ tự bắt exception (quan trọng — từ cụ thể đến tổng quát):
 * 1. [HttpRequestTimeoutException]  → [AppError.Timeout]
 * 2. [ResponseException]            → parse body → [ErrorResponseDto.toAppError()]
 * 3. [SerializationException]       → [AppError.Serialization]
 * 4. [IOException]                  → [AppError.Network]
 * 5. [Exception]                    → [AppError.Unknown]
 *
 * @param dispatcher Dispatcher để chạy [apiCall] (thường là [AppDispatchers.io]).
 * @param apiCall    Lambda suspend thực hiện request Ktor và trả về [T].
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T,
): Resource<T> = withContext(dispatcher) {
    try {
        Resource.Success(apiCall())
    } catch (e: HttpRequestTimeoutException) {
        Resource.Error(
            AppError.Timeout(
                message = e.message ?: "Request timed out",
            )
        )
    } catch (e: ResponseException) {
        // Đọc body text rồi thử parse sang ErrorResponseDto
        val errorBody = try {
            e.response.bodyAsText()
        } catch (_: Exception) {
            null
        }

        val appError = if (errorBody != null) {
            try {
                lenientJson.decodeFromString<ErrorResponseDto>(errorBody).toAppError()
            } catch (_: SerializationException) {
                // Body không phải ErrorResponseDto chuẩn — fallback theo HTTP status
                val status = e.response.status.value
                when (status) {
                    in 500..599 -> AppError.Server(status = status, message = e.message ?: "Server error")
                    401 -> AppError.SessionExpired(message = e.message ?: "Unauthorized")
                    403 -> AppError.Forbidden(message = e.message ?: "Forbidden")
                    429 -> AppError.RateLimited(retryAfterSeconds = 0L, message = e.message ?: "Too many requests")
                    423 -> AppError.AccountLocked(retryAfterSeconds = 0L, message = e.message ?: "Account locked")
                    else -> AppError.Unknown(message = "HTTP ${status}: ${e.message}")
                }
            }
        } else {
            val status = e.response.status.value
            AppError.Server(status = status, message = e.message ?: "Server error")
        }

        Resource.Error(appError)
    } catch (e: SerializationException) {
        Resource.Error(
            AppError.Serialization(
                message = e.message ?: "Failed to parse server response",
            )
        )
    } catch (e: IOException) {
        Resource.Error(
            AppError.Network(
                message = e.message ?: "No network connection",
            )
        )
    } catch (e: Exception) {
        Resource.Error(
            AppError.Unknown(
                message = e.message ?: "An unexpected error occurred",
            )
        )
    }
}

/**
 * Json instance tha thứ (lenient) dùng cho parse error body:
 * - ignoreUnknownKeys: bỏ qua field lạ mà backend có thể thêm vào
 * - isLenient: chấp nhận JSON không hoàn toàn chuẩn spec
 */
private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
