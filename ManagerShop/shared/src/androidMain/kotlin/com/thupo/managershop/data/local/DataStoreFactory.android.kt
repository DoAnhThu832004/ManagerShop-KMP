// shared/src/androidMain/kotlin/com/thupo/managershop/data/local/DataStoreFactory.android.kt
package com.thupo.managershop.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * actual Android — file lưu trong filesDir của ứng dụng.
 * Context được lấy qua KoinComponent (đã đăng ký trong ShopApplication).
 */
actual fun createDataStore(): DataStore<Preferences> {
    val context: Context = object : KoinComponent {
        val ctx: Context by inject()
    }.ctx
    return PreferenceDataStoreFactory.create(
        produceFile = {
            File(context.filesDir, DATASTORE_FILE_NAME)
        }
    )
}
