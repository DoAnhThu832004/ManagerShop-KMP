// shared/src/commonMain/kotlin/com/thupo/managershop/core/AppDispatchers.kt
package com.thupo.managershop.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Interface trừu tượng hoá các CoroutineDispatcher để dễ mock trong unit test.
 */
interface AppDispatchers {
    /** Dispatcher cho các tác vụ I/O (network, disk). */
    val io: CoroutineDispatcher

    /** Dispatcher cho tính toán CPU-bound. */
    val default: CoroutineDispatcher

    /** Dispatcher cho UI thread (Main). */
    val main: CoroutineDispatcher
}

/**
 * Triển khai mặc định dùng Dispatchers chuẩn của kotlinx.coroutines.
 * Trên Desktop JVM: Dispatchers.IO và Dispatchers.Default là thread pool,
 * Dispatchers.Main cần kotlinx-coroutines-swing (đã có trong desktopApp).
 */
class DefaultAppDispatchers : AppDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}
