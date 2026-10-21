// shared/src/jvmMain/kotlin/com/thupo/managershop/data/local/DataStoreFactory.jvm.kt
package com.thupo.managershop.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File

/**
 * actual Desktop JVM — file lưu trong thư mục user home / .managershop /
 * Thư mục được tạo tự động nếu chưa tồn tại.
 */
actual fun createDataStore(): DataStore<Preferences> {
    val appDir = File(System.getProperty("user.home"), ".managershop").apply { mkdirs() }
    return PreferenceDataStoreFactory.create(
        produceFile = { File(appDir, DATASTORE_FILE_NAME) }
    )
}
