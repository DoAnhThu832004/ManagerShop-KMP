package com.thupo.managershop.presentation.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thupo.managershop.core.Resource
import com.thupo.managershop.domain.repository.BranchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupBranchUiState(
    val name: String = "",
    val code: String = "",
    val address: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

class BranchViewModel(
    private val branchRepository: BranchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupBranchUiState())
    val uiState: StateFlow<SetupBranchUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { current ->
            val suggested = suggestBranchCode(value)
            val shouldAutoUpdateCode = current.code.isBlank() || current.code == suggestBranchCode(current.name)
            current.copy(
                name = value,
                code = if (shouldAutoUpdateCode) suggested else current.code,
                errorMessage = null,
            )
        }
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value.uppercase().trim(), errorMessage = null) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun setupInitialBranch(onSuccess: () -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        val code = state.code.trim().uppercase()

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên chi nhánh / cửa hàng") }
            return
        }
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập mã chi nhánh (ví dụ: CN01)") }
            return
        }
        if (code.length < 2 || code.length > 50) {
            _uiState.update { it.copy(errorMessage = "Mã chi nhánh phải từ 2 đến 50 ký tự") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = branchRepository.setupInitialBranch(
                code = code,
                name = name,
                address = state.address.ifBlank { null },
                phone = state.phone.ifBlank { null },
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }
                is Resource.Error -> {
                    val msg = result.appError.message.ifBlank { "Khởi tạo chi nhánh thất bại, vui lòng thử lại" }
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    companion object {
        fun suggestBranchCode(name: String): String {
            if (name.isBlank()) return ""
            val clean = name.replace(Regex("[^a-zA-Z0-9 ]"), "")
            val words = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (words.isEmpty()) return "CN01"
            val prefix = if (words.size == 1) {
                words[0].take(4).uppercase()
            } else {
                words.map { it.first().uppercaseChar() }.joinToString("").take(4)
            }
            return "${prefix}_01"
        }
    }
}
