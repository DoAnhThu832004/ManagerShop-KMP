package com.thupo.managershop.data.remote.api

import com.thupo.managershop.data.remote.dto.CreateBranchRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BranchApiTest {

    private fun createMockClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            defaultRequest {
                url("https://manager-shop.onrender.com/api/v1/")
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }
    }

    @Test
    fun setupInitialBranch_sendsCorrectJsonAndParsesResponseWithBidTokens() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/v1/branches/setup-initial", request.url.encodedPath)

            respond(
                content = """
                    {
                        "branchId": 100,
                        "branchCode": "BOUTIQUE_01",
                        "branchName": "Boutique Thu Hằng",
                        "address": "123 Cầu Giấy, Hà Nội",
                        "phone": "0987654321",
                        "accessToken": "new_access_token_with_bid",
                        "refreshToken": "new_refresh_token",
                        "tokenType": "Bearer",
                        "expiresIn": 3600
                    }
                """.trimIndent(),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val client = createMockClient(mockEngine)
        val branchApi = BranchApiImpl(client)

        val response = branchApi.setupInitialBranch(
            CreateBranchRequestDto(
                code = "BOUTIQUE_01",
                name = "Boutique Thu Hằng",
                address = "123 Cầu Giấy, Hà Nội",
                phone = "0987654321",
            )
        )

        assertEquals(100L, response.branchId)
        assertEquals("BOUTIQUE_01", response.branchCode)
        assertEquals("Boutique Thu Hằng", response.branchName)
        assertEquals("new_access_token_with_bid", response.accessToken)
    }

    @Test
    fun checkAccountStatus_sendsQueryParamAndParsesResponse() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/v1/auth/check-status", request.url.encodedPath)
            assertEquals("testuser", request.url.parameters["username"])

            respond(
                content = """
                    {
                        "username": "testuser",
                        "status": "ACTIVE",
                        "message": "Tài khoản đã được phê duyệt"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val client = createMockClient(mockEngine)
        val authApi = AuthApiImpl(client)

        val response = authApi.checkAccountStatus("testuser")

        assertEquals("testuser", response.username)
        assertEquals("ACTIVE", response.status)
    }
}
