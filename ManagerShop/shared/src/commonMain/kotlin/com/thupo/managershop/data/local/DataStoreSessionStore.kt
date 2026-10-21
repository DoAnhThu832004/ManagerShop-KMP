// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/DataStoreSessionStore.kt
package com.thupo.managershop.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Triển khai [SessionStore] dùng DataStore Preferences Multiplatform.
 * Lưu thông tin phiên KHÔNG nhạy cảm: roles, branchId, branchName.
 *
 * @param dataStore Instance DataStore<Preferences> được inject qua Koin.
 *                  Phải là singleton (không tạo nhiều instance trên cùng file).
 */
class DataStoreSessionStore(
    private val dataStore: DataStore<Preferences>,
) : SessionStore {

    // ── Keys ─────────────────────────────────────────────────────────────────

    private companion object Keys {
        /** Roles lưu dạng chuỗi phân cách bằng "," — e.g. "ADMIN,CASHIER" */
        val KEY_ROLES = stringPreferencesKey("session_roles")
        val KEY_BRANCH_ID = longPreferencesKey("session_branch_id")
        val KEY_BRANCH_NAME = stringPreferencesKey("session_branch_name")
        const val ROLES_SEPARATOR = ","
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun saveUserData(
        roles: List<String>,
        branchId: Long?,
        branchName: String?,
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_ROLES] = roles.joinToString(ROLES_SEPARATOR)
            if (branchId != null) prefs[KEY_BRANCH_ID] = branchId
            else prefs.remove(KEY_BRANCH_ID)
            if (branchName != null) prefs[KEY_BRANCH_NAME] = branchName
            else prefs.remove(KEY_BRANCH_NAME)
        }
    }

    override suspend fun clearUserData() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ROLES)
            prefs.remove(KEY_BRANCH_ID)
            prefs.remove(KEY_BRANCH_NAME)
        }
    }

    // ── Read (Flows) ──────────────────────────────────────────────────────────

    override fun getUserRoles(): Flow<List<String>> =
        dataStore.data.map { prefs ->
            prefs[KEY_ROLES]
                ?.split(ROLES_SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    override fun getBranchInfo(): Flow<Pair<Long?, String?>?> =
        dataStore.data.map { prefs ->
            val branchId = prefs[KEY_BRANCH_ID]
            val branchName = prefs[KEY_BRANCH_NAME]
            // Trả null nếu chưa có branch nào được lưu
            if (branchId == null && branchName == null) null
            else Pair(branchId, branchName)
        }
}
