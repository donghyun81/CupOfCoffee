package com.example.data

sealed class DataResult<out R> {

    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val exception: Exception) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()

    companion object {
        fun <T> success(data: T): DataResult<T> = Success(data)
        fun error(exception: Exception): DataResult<Nothing> = Error(exception)
        fun loading(): DataResult<Nothing> = Loading
    }
}

inline fun <T, R> DataResult<T>.handle(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (Exception) -> R
): R {
    return when (this) {
        is DataResult.Loading -> onLoading()
        is DataResult.Success -> onSuccess(data)
        is DataResult.Error -> onError(exception)
    }
}