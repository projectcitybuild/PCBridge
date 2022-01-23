package com.projectcitybuild.modules.nameguesser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class NameGuesserTest {

    @Test
    fun `guessClosest should return exact match first`() {
        val nameGuesser = NameGuesser()
        val result = nameGuesser.guessClosest("te", listOf(
            "test",
            "tes",
            "te",
            "t",
        ))
        assertEquals("te", result)
    }

    @Test
    fun `guessClosest should return closest match`() {
        val nameGuesser = NameGuesser()
        val result = nameGuesser.guessClosest("te", listOf(
            "test",
            "tes",
            "t",
        ))
        assertEquals("tes", result)
    }

    @Test
    fun `guessClosest should not return middle-of-string match`() {
        val nameGuesser = NameGuesser()
        val result = nameGuesser.guessClosest("name", listOf(
            "test-name",
            "name-test",
            "test-test-name",
        ))
        assertEquals("name-test", result)
    }
}