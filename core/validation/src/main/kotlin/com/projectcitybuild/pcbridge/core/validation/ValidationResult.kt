package com.projectcitybuild.pcbridge.core.validation

/**
 * The outcome of validating a value or a set of conditions.
 */
sealed interface ValidationResult<out T> {
    data class Valid<T>(
        val value: T,
    ) : ValidationResult<T>

    data class Invalid(
        val errors: List<ValidationError>,
    ) : ValidationResult<Nothing> {
        init {
            require(errors.isNotEmpty()) {
                "An invalid result must contain at least one error."
            }
        }
    }
}
