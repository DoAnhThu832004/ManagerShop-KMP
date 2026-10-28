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
 * Use case kiểm tra độ mạnh của mật khẩu đồng bộ với chuẩn [PasswordPolicyValidator] của Backend Spring Boot.
 *
 * Quy tắc:
 * - Tối thiểu 8 ký tự, tối đa 128 ký tự
 * - Chứa ít nhất 1 chữ cái in hoa (A-Z)
 * - Chứa ít nhất 1 chữ cái in thường (a-z)
 * - Chứa ít nhất 1 chữ số (0-9)
 * - Không nằm trong danh sách mật khẩu yếu/phổ biến nhất
 * - Không chứa tên đăng nhập (username)
 * - Không chứa tên tài khoản email (phần trước @)
 */
class ValidatePasswordUseCase {

    private val commonPasswords = setOf(
        "12345678", "123456789", "1234567890", "password", "password123",
        "admin123", "manager123", "qwertyuiop", "iloveyou", "welcome123",
        "11111111", "00000000", "shop1234", "shopmanager"
    )

    operator fun invoke(
        password: String,
        username: String? = null,
        email: String? = null,
    ): ValidationResult {
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

        val lowerPassword = password.lowercase()

        if (commonPasswords.contains(lowerPassword)) {
            return ValidationResult.error("Mật khẩu quá phổ biến và dễ đoán, vui lòng chọn mật khẩu phức tạp hơn")
        }

        if (!username.isNullOrBlank()) {
            val cleanUsername = username.trim().lowercase()
            if (cleanUsername.length >= 3 && lowerPassword.contains(cleanUsername)) {
                return ValidationResult.error("Mật khẩu không được chứa tên đăng nhập của bạn")
            }
        }

        if (!email.isNullOrBlank() && email.contains("@")) {
            val emailPrefix = email.substring(0, email.indexOf('@')).trim().lowercase()
            if (emailPrefix.length >= 3 && lowerPassword.contains(emailPrefix)) {
                return ValidationResult.error("Mật khẩu không được chứa phần tên của email")
            }
        }

        return ValidationResult.success()
    }
}
