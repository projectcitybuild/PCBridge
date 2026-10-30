package com.projectcitybuild.pcbridge.features.gameplay.time

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.GameTimeArgument
import com.projectcitybuild.pcbridge.platform.paper.data.GameTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class GameTimeArgumentTest {
    @Test
    fun `parses a typed time value while preserving invalid input for localization`() {
        assertEquals(GameTime("bad-time"), GameTimeArgument().convert("bad-time"))
    }

    @Test
    fun `suggests matching aliases and fixed values only when enabled`() {
        val suggestions =
            GameTimeArgument(allowFixedTime = true)
                .listSuggestions(mock<CommandContext<Any>>(), SuggestionsBuilder("@no", 0))
                .join()
                .list
                .map { it.text }

        assertEquals(listOf("@noon"), suggestions)
    }

    @Test
    fun `does not suggest fixed values for world time`() {
        val suggestions =
            GameTimeArgument()
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
            GameTimeArgument(suggestionsAfterTime = { listOf("Alex", "Notch") })
                .listSuggestions(mock<CommandContext<Any>>(), SuggestionsBuilder("noon Al", 0))
                .join()
                .list

        assertEquals("Alex", suggestions.single().text)
        assertEquals(5, suggestions.single().range.start)
    }
}
