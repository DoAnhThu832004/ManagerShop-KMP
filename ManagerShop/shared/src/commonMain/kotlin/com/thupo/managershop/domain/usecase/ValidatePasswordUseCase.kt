// shared/src/commonMain/kotlin/com/thupo/managershop/domain/usecase/ValidatePasswordUseCase.kt
package com.thupo.managershop.domain.usecase

/**
 * Kết quả xác thực dữ liệu đầu vào.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
) {
    companion object {
        fun success(): ValidationResult = ValidationResult(isValid = true, errorMessage = null)
        fun error(message: String): ValidationResult = ValidationResult(isValid = false, errorMessage = message)
    }
}

/**
 * Use case kiểm tra độ mạnh của mật khẩu theo chuẩn bảo mật hệ thống (PasswordPolicyValidator).
 *
 * Quy tắc:
 * - Tối thiểu 8 ký tự
 * - Tối đa 128 ký tự
 * - Chứa ít nhất 1 chữ cái in hoa (A-Z)
 * - Chứa ít nhất 1 chữ cái in thường (a-z)
 * - Chứa ít nhất 1 chữ số (0-9)
 */
class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult.error("Mật khẩu phải có tối thiểu 8 ký tự")
        }
        if (password.length > 128) {
            return ValidationResult.error("Mật khẩu không được vượt quá 128 ký tự")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult.error("Mật khẩu phải chứa ít nhất 1 chữ in hoa (A-Z)")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult.error("Mật khẩu phải chứa ít nhất 1 chữ in thường (a-z)")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult.error("Mật khẩu phải chứa ít nhất 1 chữ số (0-9)")
        }
        return ValidationResult.success()
    }
}
