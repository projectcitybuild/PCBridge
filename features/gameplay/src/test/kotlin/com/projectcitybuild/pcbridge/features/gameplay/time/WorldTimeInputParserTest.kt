package com.projectcitybuild.pcbridge.features.gameplay.time

import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeInputParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WorldTimeInputParserTest {
    private val parser = WorldTimeInputParser()

    @Test
    fun `parses signed tick and duration inputs`() {
        assertEquals(WorldTimeInputParser.ParsedTime(1_000L, true), parser.parse("+1h"))
        assertEquals(WorldTimeInputParser.ParsedTime(-100L, true), parser.parse("-100"))
        assertEquals(WorldTimeInputParser.ParsedTime(24_000L, true), parser.parse("+1d"))
        assertEquals(WorldTimeInputParser.ParsedTime(20L, true), parser.parse("+1s"))
        assertEquals(WorldTimeInputParser.ParsedTime(1_200L, true), parser.parse("+1m"))
    }

    @Test
    fun `parses clock inputs as absolute time`() {
        assertEquals(WorldTimeInputParser.ParsedTime(24_500L, false), parser.parse("06:30"))
        assertEquals(WorldTimeInputParser.ParsedTime(18_000L, false), parser.parse("12am"))
    }

    @Test
    fun `parses aliases into ticks`() {
        assertEquals(WorldTimeInputParser.ParsedTime(6_000L, false), parser.parse("noon"))
        assertEquals(WorldTimeInputParser.ParsedTime(6_000L, false), parser.parse("NOON"))
    }

    @Test
    fun `rejects invalid and overflowing inputs`() {
        assertNull(parser.parse("25:70"))
        assertNull(parser.parse("+9223372036854775807d"))
    }
}
