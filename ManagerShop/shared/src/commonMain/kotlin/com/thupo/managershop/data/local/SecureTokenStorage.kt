// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/SecureTokenStorage.kt
package com.thupo.managershop.data.local

/**
 * Interface trừu tượng hoá lưu trữ token bảo mật.
 * Mỗi nền tảng sẽ triển khai cơ chế mã hóa riêng:
 *   - Android: Android Keystore + AES/GCM
 *   - Desktop JVM: AES/GCM với key lưu trong java.util.prefs.Preferences
 */
interface SecureTokenStorage {
    /** Lưu cặp access/refresh token sau khi đăng nhập thành công. */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /** Đọc access token. Trả về null nếu chưa đăng nhập hoặc đã bị xóa. */
    suspend fun getAccessToken(): String?

    /** Đọc refresh token. Trả về null nếu chưa đăng nhập hoặc đã bị xóa. */
    suspend fun getRefreshToken(): String?

    /** Xóa toàn bộ token khỏi storage (đăng xuất, hết phiên). */
    suspend fun clearTokens()
}

/**
 * expect factory function — mỗi nền tảng cung cấp actual implementation.
 * Được gọi trong Koin module để cung cấp singleton [SecureTokenStorage].
 */
expect fun createSecureTokenStorage(): SecureTokenStorage
