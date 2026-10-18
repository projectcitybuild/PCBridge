package com.projectcitybuild.pcbridge.core.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ValidationResultTest {
    @Test
    fun `valid result retains its value`() {
        val result = ValidationResult.Valid("value")

        assertEquals("value", result.value)
        assertEquals(ValidationResult.Valid("value"), result)
    }

    @Test
    fun `invalid result retains its errors`() {
        val error = TestValidationError("invalid value")
        val result = ValidationResult.Invalid(listOf(error))

        assertEquals(listOf(error), result.errors)
    }

    @Test
    fun `invalid result requires at least one error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ValidationResult.Invalid(emptyList())
        }

        assertEquals("An invalid result must contain at least one error.", exception.message)
    }

    private data class TestValidationError(val message: String) : ValidationError
}
