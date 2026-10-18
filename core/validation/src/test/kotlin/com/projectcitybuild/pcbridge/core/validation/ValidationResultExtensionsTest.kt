package com.projectcitybuild.pcbridge.core.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidationResultExtensionsTest {
    @Test
    fun `map transforms a valid value`() {
        val result = ValidationResult.Valid(2).map { it * 3 }

        assertEquals(ValidationResult.Valid(6), result)
    }

    @Test
    fun `map passes an invalid result through without transforming`() {
        val invalid: ValidationResult<Int> = ValidationResult.Invalid(listOf(TestValidationError))
        var transformCalled = false

        val result = invalid.map {
            transformCalled = true
            it.toString()
        }

        assertSame(invalid, result)
        assertFalse(transformCalled)
    }

    @Test
    fun `getOrNull returns the value of a valid result`() {
        assertEquals("value", ValidationResult.Valid("value").getOrNull())
    }

    @Test
    fun `getOrNull returns null for an invalid result`() {
        val invalid: ValidationResult<String> = ValidationResult.Invalid(listOf(TestValidationError))

        assertNull(invalid.getOrNull())
    }

    @Test
    fun `success and failure checks identify their result variants`() {
        val valid = ValidationResult.Valid("value")
        val invalid: ValidationResult<String> = ValidationResult.Invalid(listOf(TestValidationError))

        assertTrue(valid.isSuccess())
        assertFalse(valid.isFailure())
        assertFalse(invalid.isSuccess())
        assertTrue(invalid.isFailure())
    }

    private object TestValidationError : ValidationError
}
