// shared/src/commonTest/kotlin/com/thupo/managershop/testutil/MockEngineBuilder.kt
package com.thupo.managershop.testutil

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tiện ích xây dựng [MockEngine] có khả năng đếm request, ghi nhận header,
 * và mô phỏng phản hồi / lỗi mạng cho các kịch bản kiểm thử Auth.
 */
class MockEngineBuilder {

    private val handlers = mutableListOf<suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData?>()
    private val requestMutex = Mutex()
    private val _recordedRequests = mutableListOf<HttpRequestData>()

    private val countMutex = Mutex()
    private var _refreshCallCount = 0

    suspend fun getRefreshCallCount(): Int = countMutex.withLock { _refreshCallCount }

    suspend fun getRecordedRequests(): List<HttpRequestData> = requestMutex.withLock {
        _recordedRequests.toList()
    }

    /**
     * Cấu hình phản hồi riêng cho endpoint `auth/refresh`.
     */
    fun onRefresh(
        delayMs: Long = 0,
        responseProvider: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
            respond(
                content = """
                    {
                        "accessToken": "new_access_token",
                        "refreshToken": "new_refresh_token",
                        "tokenType": "Bearer",
                        "expiresIn": 3600,
                        "roles": ["ADMIN"]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
    ): MockEngineBuilder {
        handlers.add { request ->
            if (request.url.encodedPath.endsWith("auth/refresh")) {
                countMutex.withLock { _refreshCallCount++ }
                if (delayMs > 0) delay(delayMs)
                responseProvider(this, request)
            } else null
        }
        return this
    }

    /**
     * Thêm handler tuỳ biến cho các request khác.
     */
    fun addHandler(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData?,
    ): MockEngineBuilder {
        handlers.add(handler)
        return this
    }

    /**
     * Tạo [MockEngine] hoàn chỉnh.
     */
    fun build(): MockEngine {
        return MockEngine { request ->
            requestMutex.withLock { _recordedRequests.add(request) }
            for (handler in handlers) {
                val response = handler(this, request)
                if (response != null) return@MockEngine response
            }
            // Mặc định trả về 200 OK JSON rỗng
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
