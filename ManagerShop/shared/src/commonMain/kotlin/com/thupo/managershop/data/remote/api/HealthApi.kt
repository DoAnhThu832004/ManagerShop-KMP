// shared/src/commonMain/kotlin/com/thupo/managershop/data/remote/api/HealthApi.kt
package com.thupo.managershop.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlin.time.measureTimedValue

/**
 * Interface cho API kiểm tra trạng thái sức khoẻ và warm-up backend Render.
 */
interface HealthApi {
    /**
     * Gọi endpoint health check: GET /api/v1/health
     */
    suspend fun checkHealth(): HttpResponse

    /**
     * Gửi request warm-up và đo lường thời gian phản hồi (round-trip time tính bằng milliseconds).
     * @return Thời gian phản hồi tính bằng ms nếu server trả về status 2xx, hoặc null nếu gặp lỗi / không phản hồi.
     */
    suspend fun warmUpServer(): Long?
}

/**
 * Triển khai chuẩn của [HealthApi] sử dụng Ktor [HttpClient].
 */
class HealthApiImpl(
    private val httpClient: HttpClient,
) : HealthApi {

    override suspend fun checkHealth(): HttpResponse {
        return httpClient.get("health")
    }

    override suspend fun warmUpServer(): Long? {
        return try {
            val (response, duration) = measureTimedValue {
                checkHealth()
            }
            if (response.status.value in 200..299) {
                duration.inWholeMilliseconds
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }
}
