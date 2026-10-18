package com.projectcitybuild.pcbridge.core.validation

/**
 * Transforms a successfully validated value.
 * Invalid results pass through unchanged.
 */
inline fun <T, R> ValidationResult<T>.map(transform: (T) -> R): ValidationResult<R> = when (this) {
    is ValidationResult.Valid -> {
        ValidationResult.Valid(transform(value))
    }
    is ValidationResult.Invalid -> {
        this
    }
}

inline fun <T, R> ValidationResult<T>.flatMap(transform: (T) -> ValidationResult<R>): ValidationResult<R> = when (this) {
    is ValidationResult.Valid -> transform(value)
    is ValidationResult.Invalid -> this
}

/**
 * Returns the validated value, or null when validation failed.
 */
fun <T> ValidationResult<T>.getOrNull(): T? = when (this) {
    is ValidationResult.Valid -> value
    is ValidationResult.Invalid -> null
}

fun <T> ValidationResult<T>.errors(): List<ValidationError> = when (this) {
    is ValidationResult.Valid -> emptyList()
    is ValidationResult.Invalid -> errors
}

fun <T> ValidationResult<T>.isSuccess(): Boolean = this is ValidationResult.Valid

fun <T> ValidationResult<T>.isFailure(): Boolean = this is ValidationResult.Invalid
