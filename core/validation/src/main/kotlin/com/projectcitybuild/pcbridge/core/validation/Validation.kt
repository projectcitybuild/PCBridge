package com.projectcitybuild.pcbridge.core.validation

/**
 * Runs all validators and accumulates their errors.
 *
 * Every validator runs, even if an earlier validator fails.
 * Use this only when the checks are independent and safe to run
 * regardless of the outcome of the other checks.
 */
fun <T> validateAll(
    input: T,
    validators: Iterable<Validator<T>>,
): ValidationResult<Unit> {
    val errors = buildList {
        validators.forEach { validator ->
            when (val result = validator.validate(input)) {
                is ValidationResult.Valid -> Unit
                is ValidationResult.Invalid -> addAll(result.errors)
            }
        }
    }

    return if (errors.isEmpty()) {
        ValidationResult.Valid(Unit)
    } else {
        ValidationResult.Invalid(errors)
    }
}
