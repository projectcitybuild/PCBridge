package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlayerTimeCommandInputArgumentParserTest {
    private val server = mock<Server>()
    private val parser = PlayerTimeCommandInputArgumentParser(server)

    @Test
    fun `parses fixed clock input and optional player name`() {
        val reader = StringReader("@06:30 Alex")

        val nativeValue = parser.getNativeType().parse(reader)

        assertEquals(
            PlayerTimeCommandInput("@06:30", "Alex"),
            parser.convert(nativeValue),
        )
    }

    @Test
    fun `leaves target player absent when input has one token`() {
        assertEquals(
            PlayerTimeCommandInput("noon", null),
            parser.convert("noon"),
        )
    }

    @Test
    fun `suggests player names after the time value`() {
        val player = mock<Player>()
        whenever(player.name).thenReturn("Alex")
        whenever(server.onlinePlayers).thenReturn(listOf(player))

        val suggestions = parser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("noon Al", 0),
        ).get().list

        assertEquals(listOf("Alex"), suggestions.map { it.text })
        assertEquals(5, suggestions.single().range.start)
    }
}
