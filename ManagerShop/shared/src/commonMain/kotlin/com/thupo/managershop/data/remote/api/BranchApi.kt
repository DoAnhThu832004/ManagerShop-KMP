package com.thupo.managershop.data.remote.api

import com.thupo.managershop.data.remote.dto.CreateBranchRequestDto
import com.thupo.managershop.data.remote.dto.SetupBranchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Interface gọi các API liên quan đến Chi nhánh (Branch Management).
 */
interface BranchApi {
    /**
     * Khởi tạo chi nhánh gốc đầu tiên cho Chủ shop mới (POST branches/setup-initial).
     * Trả về thông tin chi nhánh kèm cặp Token mới đã mang claim 'bid'.
     */
    suspend fun setupInitialBranch(request: CreateBranchRequestDto): SetupBranchResponseDto
}

class BranchApiImpl(
    private val httpClient: HttpClient,
) : BranchApi {

    override suspend fun setupInitialBranch(request: CreateBranchRequestDto): SetupBranchResponseDto {
        return httpClient.post("branches/setup-initial") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
