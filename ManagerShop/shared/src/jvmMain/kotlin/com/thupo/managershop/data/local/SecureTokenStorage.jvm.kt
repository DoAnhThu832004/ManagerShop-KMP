// shared/src/jvmMain/kotlin/com/thupo/managershop/data/local/SecureTokenStorage.jvm.kt
package com.thupo.managershop.data.local

import java.security.SecureRandom
import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Triển khai Desktop JVM — lưu token mã hoá bằng AES-256-GCM.
 *
 * Cơ chế bảo mật:
 * - SecretKey (AES-256) được sinh ngẫu nhiên lần đầu, rồi lưu mã hoá trong
 *   java.util.prefs.Preferences (user node — tầm với hệ điều hành của user hiện tại).
 * - Dữ liệu (access/refresh token) được mã hoá với key đó + IV ngẫu nhiên mỗi lần.
 * - Trên Windows, Preferences lưu trong HKCU registry; trên Linux/macOS lưu file ~/.java/.userPrefs.
 *
 * Mức độ bảo mật: Phụ thuộc vào việc bảo vệ tài khoản OS. Phù hợp cho POS Desktop
 * không chạy trên máy chủ chia sẻ. Production-grade cần tích hợp OS credential store
 * (Windows Credential Manager, macOS Keychain) ở giai đoạn sau.
 */
internal class DesktopSecureTokenStorage : SecureTokenStorage {

    private val osPrefs: Preferences =
        Preferences.userRoot().node("/com/thupo/managershop/tokens")

    // ── Key management ────────────────────────────────────────────────────────

    private fun getOrCreateKey(): SecretKey {
        val stored = osPrefs.get(PREF_MASTER_KEY, null)
        if (stored != null) {
            val keyBytes = Base64.getDecoder().decode(stored)
            return SecretKeySpec(keyBytes, "AES")
        }
        // Sinh key mới
        val gen = KeyGenerator.getInstance("AES")
        gen.init(256, SecureRandom())
        val key = gen.generateKey()
        osPrefs.put(PREF_MASTER_KEY, Base64.getEncoder().encodeToString(key.encoded))
        osPrefs.flush()
        return key
    }

    // ── Encrypt / Decrypt ─────────────────────────────────────────────────────

    private fun encrypt(plaintext: String): String {
        val iv = ByteArray(GCM_IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LEN_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val enc = Base64.getEncoder()
        return enc.encodeToString(iv) + IV_SEPARATOR + enc.encodeToString(ciphertext)
    }

    private fun decrypt(encoded: String): String? {
        return try {
            val parts = encoded.split(IV_SEPARATOR)
            if (parts.size != 2) return null
            val dec = Base64.getDecoder()
            val iv = dec.decode(parts[0])
            val ciphertext = dec.decode(parts[1])
            val cipher = Cipher.getInstance(AES_GCM)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(GCM_TAG_LEN_BITS, iv),
            )
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    // ── SecureTokenStorage impl ───────────────────────────────────────────────

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        osPrefs.put(PREF_ACCESS_TOKEN, encrypt(accessToken))
        osPrefs.put(PREF_REFRESH_TOKEN, encrypt(refreshToken))
        osPrefs.flush()
    }

    override suspend fun getAccessToken(): String? =
        osPrefs.get(PREF_ACCESS_TOKEN, null)?.let { decrypt(it) }

    override suspend fun getRefreshToken(): String? =
        osPrefs.get(PREF_REFRESH_TOKEN, null)?.let { decrypt(it) }

    override suspend fun clearTokens() {
        osPrefs.remove(PREF_ACCESS_TOKEN)
        osPrefs.remove(PREF_REFRESH_TOKEN)
        osPrefs.flush()
    }

    private companion object {
        const val AES_GCM = "AES/GCM/NoPadding"
        const val GCM_IV_LEN = 12
        const val GCM_TAG_LEN_BITS = 128
        const val IV_SEPARATOR = ":"
        const val PREF_MASTER_KEY = "master_key"
        const val PREF_ACCESS_TOKEN = "access_token"
        const val PREF_REFRESH_TOKEN = "refresh_token"
    }
}

/** actual factory cho Desktop JVM — không cần Context. */
actual fun createSecureTokenStorage(): SecureTokenStorage = DesktopSecureTokenStorage()
