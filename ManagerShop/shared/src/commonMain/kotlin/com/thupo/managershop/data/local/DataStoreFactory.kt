// shared/src/commonMain/kotlin/com/thupo/managershop/data/local/DataStoreFactory.kt
package com.thupo.managershop.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * expect factory tạo DataStore<Preferences> singleton.
 * Mỗi nền tảng quyết định thư mục lưu file `.preferences_pb`.
 */
expect fun createDataStore(): DataStore<Preferences>

/** Tên file DataStore — phải kết thúc bằng .preferences_pb */
internal const val DATASTORE_FILE_NAME = "managershop_session.preferences_pb"
