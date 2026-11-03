package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeInputParser.ParsedTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WorldTimeInputParserTest {
    private val parser = WorldTimeInputParser()

    @Test
    fun `parses aliases without case sensitivity`() {
        assertEquals(ParsedTime(6_000L, false), parser.parse(" Noon "))
        assertEquals(ParsedTime(23_000L, false), parser.parse("DAWN"))
    }

    @Test
    fun `parses numeric ticks and optional suffixes`() {
        assertEquals(ParsedTime(1_200L, false), parser.parse("1200"))
        assertEquals(ParsedTime(1_200L, false), parser.parse("1200ticks"))
        assertEquals(ParsedTime(1_200L, false), parser.parse("1200t"))
    }

    @Test
    fun `parses signed relative ticks and durations`() {
        assertEquals(ParsedTime(50L, true), parser.parse("+50"))
        assertEquals(ParsedTime(-50L, true), parser.parse("-50"))
        assertEquals(ParsedTime(48_000L, false), parser.parse("2d"))
        assertEquals(ParsedTime(3_000L, false), parser.parse("3h"))
        assertEquals(ParsedTime(2_400L, false), parser.parse("2m"))
        assertEquals(ParsedTime(60L, false), parser.parse("3s"))
    }

    @Test
    fun `parses clock times in 24-hour and 12-hour formats`() {
        assertEquals(ParsedTime(24_500L, false), parser.parse("06:30"))
        assertEquals(ParsedTime(24_500L, false), parser.parse("6:30am"))
        assertEquals(ParsedTime(36_500L, false), parser.parse("6:30pm"))
        assertEquals(ParsedTime(18_000L, false), parser.parse("12am"))
        assertEquals(ParsedTime(30_000L, false), parser.parse("12pm"))
    }

    @Test
    fun `rejects invalid times and overflowing values`() {
        listOf(
            "",
            "unknown",
            "24:00",
            "13:00pm",
            "1:60am",
            "999999999999999999999999999",
            "999999999999999999999999999d",
        ).forEach { input ->
            assertNull(parser.parse(input), "Unexpectedly parsed: $input")
        }
    }
}
