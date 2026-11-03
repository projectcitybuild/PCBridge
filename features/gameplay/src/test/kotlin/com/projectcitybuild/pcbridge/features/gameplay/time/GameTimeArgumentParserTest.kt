package com.projectcitybuild.pcbridge.features.gameplay.time

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.GameTime
import com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments.GameTimeArgumentParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class GameTimeArgumentParserTest {
    @Test
    fun `parses a typed time value while preserving invalid input for localization`() {
        assertEquals(GameTime("bad-time"), GameTimeArgumentParser().convert("bad-time"))
    }

    @Test
    fun `suggests matching aliases and fixed values only when enabled`() {
        val suggestions =
            GameTimeArgumentParser(allowFixedTime = true)
                .listSuggestions(mock<CommandContext<Any>>(), SuggestionsBuilder("@no", 0))
                .join()
                .list
                .map { it.text }

        assertEquals(listOf("@noon"), suggestions)
    }

    @Test
    fun `does not suggest fixed values for world time`() {
        val suggestions =
            GameTimeArgumentParser()
                .listSuggestions(mock<CommandContext<Any>>(), SuggestionsBuilder("", 0))
                .join()
                .list
                .map { it.text }

        assertTrue(suggestions.contains("noon"))
        assertFalse(suggestions.any { it.startsWith("@") })
    }

    @Test
    fun `suggests target names after the time value`() {
        val suggestions =
            GameTimeArgumentParser(suggestionsAfterTime = { listOf("Alex", "Notch") })
                .listSuggestions(mock<CommandContext<Any>>(), SuggestionsBuilder("noon Al", 0))
                .join()
                .list

        assertEquals("Alex", suggestions.single().text)
        assertEquals(5, suggestions.single().range.start)
    }
}
