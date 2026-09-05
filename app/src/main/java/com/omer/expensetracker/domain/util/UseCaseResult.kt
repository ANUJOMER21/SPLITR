package com.omer.expensetracker.domain.util

/** Explicit success/failure result so the UI never has to guess why a use case failed. */
sealed interface UseCaseResult<out T> {
    data class Success<T>(val data: T) : UseCaseResult<T>
    data class Failure(val message: String) : UseCaseResult<Nothing>
}

inline fun <T> UseCaseResult<T>.onSuccess(block: (T) -> Unit): UseCaseResult<T> {
    if (this is UseCaseResult.Success) block(data)
    return this
}

inline fun <T> UseCaseResult<T>.onFailure(block: (String) -> Unit): UseCaseResult<T> {
    if (this is UseCaseResult.Failure) block(message)
    return this
}
