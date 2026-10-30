package com.projectcitybuild.pcbridge.features.gameplay.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandInputParserTest {
    @Test
    fun `splits an online player from personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("noon", "Alex"),
            PlayerTimeCommandInputParser.parse("noon Alex", setOf("Alex")),
        )
    }

    @Test
    fun `splits a missing player from valid personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("noon", "Unknown"),
            PlayerTimeCommandInputParser.parse("noon Unknown", emptySet()),
        )
    }

    @Test
    fun `preserves invalid personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("invalid input", null),
            PlayerTimeCommandInputParser.parse("invalid input", emptySet()),
        )
    }

    @Test
    fun `splits a world selector from valid world time input`() {
        assertEquals(
            WorldTimeCommandInput("noon", "*"),
            WorldTimeCommandInputParser.parse("noon *") { it == "noon" },
        )
    }

    @Test
    fun `preserves invalid world time input`() {
        assertEquals(
            WorldTimeCommandInput("invalid input", null),
            WorldTimeCommandInputParser.parse("invalid input") { false },
        )
    }
}
