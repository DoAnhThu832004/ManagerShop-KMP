// shared/src/commonTest/kotlin/com/thupo/managershop/data/remote/api/HealthApiTest.kt
package com.thupo.managershop.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HealthApiTest {

    @Test
    fun `warmUpServer returns ping duration when server responds with 200 OK`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/health", request.url.encodedPath)
            respond(
                content = """{"status":"UP"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val client = HttpClient(mockEngine) {
            defaultRequest {
                url("https://manager-shop.onrender.com/api/v1/")
            }
        }

        val healthApi = HealthApiImpl(client)
        val pingMs = healthApi.warmUpServer()

        assertNotNull(pingMs)
    }

    @Test
    fun `warmUpServer returns null when server returns 500 Internal Server Error`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
            )
        }

        val client = HttpClient(mockEngine) {
            expectSuccess = true
            defaultRequest {
                url("https://manager-shop.onrender.com/api/v1/")
            }
        }

        val healthApi = HealthApiImpl(client)
        val pingMs = healthApi.warmUpServer()

        assertNull(pingMs)
    }
}
