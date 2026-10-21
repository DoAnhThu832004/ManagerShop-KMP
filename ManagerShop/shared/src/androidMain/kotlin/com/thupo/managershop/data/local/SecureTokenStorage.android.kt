// shared/src/androidMain/kotlin/com/thupo/managershop/data/local/SecureTokenStorage.android.kt
package com.thupo.managershop.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Triển khai Android — lưu token mã hoá bằng Android Keystore + AES/GCM/NoPadding.
 *
 * Cơ chế bảo mật:
 * - SecretKey được sinh và lưu TRONG Android Keystore (hardware-backed khi có TEE).
 * - Dữ liệu được mã hoá với AES-256-GCM, IV ngẫu nhiên mỗi lần ghi.
 * - Ciphertext + IV lưu qua android.content.SharedPreferences dưới dạng Base64.
 *
 * Lý do không dùng EncryptedSharedPreferences: thư viện security-crypto chỉ có
 * alpha artifact trong cache; Android Keystore thuần không cần dependency thêm.
 */
internal class AndroidSecureTokenStorage(context: Context) : SecureTokenStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Android Keystore key management ──────────────────────────────────────

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    // ── Encrypt / Decrypt ─────────────────────────────────────────────────────

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        // Format: base64(iv) + ":" + base64(ciphertext)
        return Base64.encodeToString(iv, Base64.NO_WRAP) + IV_SEPARATOR +
                Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String? {
        return try {
            val parts = encoded.split(IV_SEPARATOR)
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
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
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(refreshToken))
            .apply()
    }

    override suspend fun getAccessToken(): String? =
        prefs.getString(KEY_ACCESS_TOKEN, null)?.let { decrypt(it) }

    override suspend fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH_TOKEN, null)?.let { decrypt(it) }

    override suspend fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "managershop_token_key"
        const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
        const val GCM_TAG_LEN_BITS = 128
        const val IV_SEPARATOR = ":"
        const val PREFS_NAME = "managershop_secure_prefs"
        const val KEY_ACCESS_TOKEN = "enc_access_token"
        const val KEY_REFRESH_TOKEN = "enc_refresh_token"
    }
}

/**
 * actual factory — Koin inject Context qua KoinComponent.
 * Gọi trong [storageModule] với `single { createSecureTokenStorage() }`.
 */
actual fun createSecureTokenStorage(): SecureTokenStorage {
    // Dùng KoinComponent để lấy Context mà không cần truyền param
    return object : KoinComponent {
        val context: Context by inject()
    }.let { AndroidSecureTokenStorage(it.context) }
}
