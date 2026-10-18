package com.projectcitybuild.pcbridge.core.validation

fun interface Validator<in T> {
    fun validate(input: T): ValidationResult<Unit>
}
