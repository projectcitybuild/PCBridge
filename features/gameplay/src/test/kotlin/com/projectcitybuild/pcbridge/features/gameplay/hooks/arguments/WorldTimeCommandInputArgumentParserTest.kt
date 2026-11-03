package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WorldTimeCommandInputArgumentParserTest {
    private val server = mock<Server>()
    private val parser = WorldTimeCommandInputArgumentParser(server)

    @Test
    fun `parses time and all-world selector from one command input`() {
        val reader = StringReader("06:30 @all")

        val nativeValue = parser.getNativeType().parse(reader)

        assertEquals(
            WorldTimeCommandInput("06:30", WorldSelector.All),
            parser.convert(nativeValue),
        )
    }

    @Test
    fun `defaults to current world when selector is omitted`() {
        assertEquals(
            WorldTimeCommandInput("noon", WorldSelector.Current),
            parser.convert("noon"),
        )
    }

    @Test
    fun `suggests world selectors after the time input`() {
        val world = mock<World>()
        whenever(world.name).thenReturn("nether")
        whenever(server.worlds).thenReturn(listOf(world))

        val suggestions = parser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("noon @", 0),
        ).get().list.map { it.text }

        assertEquals(listOf("@all", "nether"), suggestions)
    }
}
