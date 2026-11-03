package com.projectcitybuild.pcbridge.features.gameplay.time

import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeCommandInput
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeCommandInputParser
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldSelectParser
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldSelector
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeCommandInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandInputParserTest {
    @Test
    fun `splits an online player from personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("noon", "Alex"),
            PlayerTimeCommandInputParser.parse("noon Alex"),
        )
    }

    @Test
    fun `splits a missing player from valid personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("noon", "Unknown"),
            PlayerTimeCommandInputParser.parse("noon Unknown"),
        )
    }

    @Test
    fun `preserves invalid personal time input`() {
        assertEquals(
            PlayerTimeCommandInput("invalid", "input"),
            PlayerTimeCommandInputParser.parse("invalid input"),
        )
    }

    @Test
    fun `splits a world selector from valid world time input`() {
        assertEquals(
            WorldTimeCommandInput("noon", WorldSelector.All),
            WorldSelectParser.parse("noon *"),
        )
    }

    @Test
    fun `parses a named world selector without validating the time`() {
        assertEquals(
            WorldTimeCommandInput("06:30", WorldSelector.Named("nether")),
            WorldSelectParser.parse("06:30 nether"),
        )
    }

    @Test
    fun `uses the default world when no selector is provided`() {
        assertEquals(
            WorldTimeCommandInput("noon", WorldSelector.Current),
            WorldSelectParser.parse("noon"),
        )
    }

    @Test
    fun `preserves malformed input for command feedback`() {
        assertEquals(
            WorldTimeCommandInput("invalid", WorldSelector.Named("input")),
            WorldSelectParser.parse("invalid input"),
        )
    }
}
