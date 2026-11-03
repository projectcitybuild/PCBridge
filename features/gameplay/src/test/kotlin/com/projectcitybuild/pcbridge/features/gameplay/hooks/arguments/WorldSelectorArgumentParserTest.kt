package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WorldSelectorArgumentParserTest {
    private val server = mock<Server>()
    private val parser = WorldSelectorArgumentParser(server)

    @Test
    fun `converts empty selector to current world`() {
        assertEquals(WorldSelector.Current, parser.convert(""))
    }

    @Test
    fun `converts all selector and world names`() {
        assertEquals(WorldSelector.All, parser.convert("@all"))
        assertEquals(WorldSelector.Named("nether"), parser.convert("nether"))
    }

    @Test
    fun `native argument accepts the at sign in all selector`() {
        val reader = StringReader("@all")

        val nativeValue = parser.getNativeType().parse(reader)

        assertEquals("@all", nativeValue)
        assertFalse(reader.canRead())
        assertEquals(WorldSelector.All, parser.convert(nativeValue))
    }

    @Test
    fun `suggests all worlds and the all selector`() {
        val world = mock<World>()
        whenever(world.name).thenReturn("nether")
        whenever(server.worlds).thenReturn(listOf(world))

        val suggestions = parser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("", 0),
        ).get().list.map { it.text }

        assertEquals(listOf("@all", "nether"), suggestions)
    }

    @Test
    fun `world name argument suggests matching worlds`() {
        val world = mock<World>()
        whenever(world.name).thenReturn("nether")
        whenever(server.worlds).thenReturn(listOf(world))
        val worldNameParser = WorldNameArgumentParser(server)

        val suggestions = worldNameParser.listSuggestions(
            mock<CommandContext<Any>>(),
            SuggestionsBuilder("net", 0),
        ).get().list.map { it.text }

        assertEquals(listOf("nether"), suggestions)
    }
}
