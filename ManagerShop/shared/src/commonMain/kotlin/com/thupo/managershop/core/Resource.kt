// shared/src/commonMain/kotlin/com/thupo/managershop/core/Resource.kt
package com.thupo.managershop.core

/**
 * Generic wrapper for UI state / data layer responses.
 *
 * Usage:
 *   when (result) {
 *       is Resource.Success -> result.data
 *       is Resource.Error   -> result.appError
 *       is Resource.Loading -> show spinner
 *   }
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val appError: AppError) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
