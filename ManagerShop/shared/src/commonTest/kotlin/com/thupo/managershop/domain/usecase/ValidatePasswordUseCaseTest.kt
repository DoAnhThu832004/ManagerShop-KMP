package com.thupo.managershop.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatePasswordUseCaseTest {

    private val validator = ValidatePasswordUseCase()

    @Test
    fun validPassword_returnsSuccess() {
        val result = validator("StrongPass123@")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun passwordShorterThan8Chars_returnsError() {
        val result = validator("Pass1")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu phải có tối thiểu 8 ký tự", result.errorMessage)
    }

    @Test
    fun passwordLongerThan128Chars_returnsError() {
        val longPassword = "A1" + "a".repeat(127)
        val result = validator(longPassword)
        assertFalse(result.isValid)
        assertEquals("Mật khẩu không được vượt quá 128 ký tự", result.errorMessage)
    }

    @Test
    fun passwordMissingUppercase_returnsError() {
        val result = validator("password123")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ in hoa (A-Z)", result.errorMessage)
    }

    @Test
    fun passwordMissingLowercase_returnsError() {
        val result = validator("PASSWORD123")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ in thường (a-z)", result.errorMessage)
    }

    @Test
    fun passwordMissingDigit_returnsError() {
        val result = validator("PasswordNoDigits")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ số (0-9)", result.errorMessage)
    }

    @Test
    fun passwordInCommonPasswords_returnsError() {
        val result = validator("Password123")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu quá phổ biến và dễ đoán, vui lòng chọn mật khẩu phức tạp hơn", result.errorMessage)
    }

    @Test
    fun passwordContainingUsername_returnsError() {
        val result = validator("HelloThuhang123@", username = "thuhang")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu không được chứa tên đăng nhập của bạn", result.errorMessage)
    }

    @Test
    fun passwordContainingEmailPrefix_returnsError() {
        val result = validator("HelloThuhang123@", email = "thuhang@gmail.com")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu không được chứa phần tên của email", result.errorMessage)
    }
}
