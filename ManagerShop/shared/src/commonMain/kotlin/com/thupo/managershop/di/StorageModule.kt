// shared/src/commonMain/kotlin/com/thupo/managershop/di/StorageModule.kt
package com.thupo.managershop.di

import com.thupo.managershop.core.AppDispatchers
import com.thupo.managershop.data.local.DataStoreSessionStore
import com.thupo.managershop.data.local.SecureTokenStorage
import com.thupo.managershop.data.local.SessionStore
import com.thupo.managershop.data.local.TokenManager
import com.thupo.managershop.data.local.createDataStore
import com.thupo.managershop.data.local.createSecureTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Koin module cung cấp các dependency tầng Storage:
 * - DataStore<Preferences>
 * - SecureTokenStorage (Android Keystore / Desktop OS Preferences)
 * - SessionStore (DataStoreSessionStore)
 * - TokenManager (Singleton quản lý trạng thái phiên đăng nhập)
 */
val storageModule = module {
    single { createDataStore() }
    single<SecureTokenStorage> { createSecureTokenStorage() }
    single<SessionStore> { DataStoreSessionStore(dataStore = get()) }
    single {
        val dispatchers: AppDispatchers = get()
        val appScope = CoroutineScope(SupervisorJob() + dispatchers.io)
        TokenManager(
            secureStorage = get(),
            sessionStore = get(),
            appScope = appScope,
        )
    }
}
