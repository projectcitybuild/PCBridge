package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.GameTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class GameTimeArgumentParserTest {
    private val parser = GameTimeArgumentParser()

    @Test
    fun `converts native input to game time`() {
        assertEquals(GameTime("noon"), parser.convert("noon"))
    }

    @Test
    fun `suggests matching aliases and time values`() {
        val suggestions = parser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("NO", 0),
        ).get().list.map { it.text }

        assertEquals(listOf("noon"), suggestions)
    }

    @Test
    fun `suggests signed examples for relative time`() {
        val suggestions = parser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("+", 0),
        ).get().list.map { it.text }

        assertEquals(listOf("+1000", "+12h"), suggestions)
    }
}
