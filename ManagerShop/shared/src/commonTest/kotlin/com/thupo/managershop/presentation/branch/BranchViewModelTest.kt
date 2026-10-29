package com.thupo.managershop.presentation.branch

import com.thupo.managershop.core.AppError
import com.thupo.managershop.core.Resource
import com.thupo.managershop.data.remote.dto.SetupBranchResponseDto
import com.thupo.managershop.domain.repository.BranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeBranchRepository : BranchRepository {
    var setupResult: Resource<SetupBranchResponseDto> = Resource.Success(
        SetupBranchResponseDto(
            branchId = 10L,
            branchCode = "CN_01",
            branchName = "Boutique Thu Hằng",
            address = "123 Cầu Giấy",
            phone = "0987654321",
            accessToken = "new_token_with_bid",
            refreshToken = "new_refresh_token",
            expiresIn = 3600L,
        )
    )

    var lastCode: String? = null
    var lastName: String? = null

    override suspend fun setupInitialBranch(
        code: String,
        name: String,
        address: String?,
        phone: String?,
    ): Resource<SetupBranchResponseDto> {
        lastCode = code
        lastName = name
        return setupResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BranchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeBranchRepository
    private lateinit var viewModel: BranchViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBranchRepository()
        viewModel = BranchViewModel(branchRepository = fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onNameChange_updatesNameAndAutoSuggestsCode() {
        viewModel.onNameChange("Boutique Thu Hang")

        assertEquals("Boutique Thu Hang", viewModel.uiState.value.name)
        assertEquals("BTH_01", viewModel.uiState.value.code)
    }

    @Test
    fun onCodeChange_forcesUppercase() {
        viewModel.onCodeChange("cn_hanoi")

        assertEquals("CN_HANOI", viewModel.uiState.value.code)
    }

    @Test
    fun setupInitialBranch_withBlankName_setsErrorWithoutCallingRepo() = runTest(testDispatcher) {
        viewModel.onNameChange("")
        viewModel.onCodeChange("CN01")

        var callbackCalled = false
        viewModel.setupInitialBranch { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertEquals("Vui lòng nhập tên chi nhánh / cửa hàng", viewModel.uiState.value.errorMessage)
        assertNull(fakeRepository.lastName)
    }

    @Test
    fun setupInitialBranch_success_invokesCallback() = runTest(testDispatcher) {
        viewModel.onNameChange("Boutique Thu Hang")
        viewModel.onCodeChange("CN01")

        var callbackCalled = false
        viewModel.setupInitialBranch { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("CN01", fakeRepository.lastCode)
        assertEquals("Boutique Thu Hang", fakeRepository.lastName)
    }

    @Test
    fun setupInitialBranch_failure_setsErrorMessage() = runTest(testDispatcher) {
        fakeRepository.setupResult = Resource.Error(
            AppError.Conflict("Mã chi nhánh đã tồn tại trong hệ thống")
        )

        viewModel.onNameChange("Boutique Thu Hang")
        viewModel.onCodeChange("CN01")

        var callbackCalled = false
        viewModel.setupInitialBranch { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals("Mã chi nhánh đã tồn tại trong hệ thống", viewModel.uiState.value.errorMessage)
    }
}
