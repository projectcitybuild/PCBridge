package com.projectcitybuild.pcbridge.features.gameplay.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GameTimeTest {
    @Test
    fun `parses aliases, numeric ticks, and clock values`() {
        assertEquals(6_000L, GameTime.parseTicks("Noon"))
        assertEquals(50L, GameTime.parseTicks("50ticks"))
        assertEquals(12_000L, GameTime.parseTicks("6pm"))
        assertEquals(500L, GameTime.parseTicks("06:30"))
    }

    @Test
    fun `preserves full tick amounts for time additions`() {
        assertEquals(24_000L, GameTime.parseAddTicks("24000"))
        assertEquals(24_000L, GameTime.parseAddTicks("24000ticks"))
        assertEquals(6_000L, GameTime.parseAddTicks("noon"))
    }

    @Test
    fun `rejects invalid clock values and malformed punctuation`() {
        assertNull(GameTime.parseTicks("25:00"))
        assertNull(GameTime.parseTicks("12:60am"))
        assertNull(GameTime.parseTicks("6pm!"))
        assertNull(GameTime.parseTicks("@@noon"))
    }

    @Test
    fun `parses fixed and relative personal time`() {
        assertEquals(PlayerTimeInput(6_000L, relative = true), GameTime.parsePlayerTime("noon"))
        assertEquals(PlayerTimeInput(6_000L, relative = false), GameTime.parsePlayerTime("@noon"))
    }
}
