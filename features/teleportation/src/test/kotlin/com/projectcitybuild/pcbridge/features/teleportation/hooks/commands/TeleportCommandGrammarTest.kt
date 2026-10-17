package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class TeleportCommandGrammarTest {
    private val player =
        mock<Player>().also {
            whenever(it.name).thenReturn("Notch")
        }
    private val reservedNamePlayer =
        mock<Player>().also {
            whenever(it.name).thenReturn("accept")
        }
    private val server =
        mock<Server>().also {
            whenever(it.onlinePlayers).thenReturn(listOf(player, reservedNamePlayer))
        }
    private val source = mock<CommandSourceStack>()
    private val sender =
        mock<CommandSender>().also {
            whenever(it.hasPermission(any<String>())).thenReturn(true)
        }
    private val plugin = mock<Plugin>()
    private val application = mock<TeleportApplication>()
    private val random = TeleportRandomCommand(plugin, application)
    private val dispatcher =
        CommandDispatcher<CommandSourceStack>().apply {
            whenever(source.sender).thenReturn(sender)
            root.addChild(
                TeleportCommand(
                    plugin = plugin,
                    server = server,
                    application = application,
                    to = TeleportToCommand(plugin, server, application),
                    here = TeleportHereCommand(plugin, server, application),
                    send = TeleportSendCommand(plugin, server, application),
                    all = TeleportAllCommand(plugin, server, application),
                    position = TeleportPosCommand(plugin, server, application),
                    random = random,
                    back = TeleportBackCommand(plugin, server, application),
                    accept = TeleportAcceptCommand(plugin, server, application),
                    deny = TeleportDenyCommand(plugin, server, application),
                    cancel = TeleportCancelCommand(plugin, application),
                ).literal(),
            )
            TeleportLegacyCommands(plugin, server, application)
                .commands()
                .forEach { root.addChild(it.literal()) }
        }

    @Test
    fun `to options parse in either valid order`() {
        listOf(
            "tp to Notch",
            "tp to Notch --force",
            "tp to Notch --silent",
            "tp to Notch --force --silent",
            "tp to Notch --silent --force",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    @Test
    fun `all request and force flags parse with or without an explicit target`() {
        listOf(
            "tp all",
            "tp all --silent",
            "tp all --force",
            "tp all --force --silent",
            "tp all --silent --force",
            "tp all Notch",
            "tp all Notch --silent",
            "tp all Notch --force",
            "tp all Notch --force --silent",
            "tp all Notch --silent --force",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    @Test
    fun `send and here accept request, force, silent and combined flags`() {
        listOf(
            "tp send Notch accept",
            "tp send Notch accept --force",
            "tp send Notch accept --silent",
            "tp send Notch accept --force --silent",
            "tp send Notch accept --silent --force",
            "tp here Notch",
            "tp here Notch --force",
            "tp here Notch --silent",
            "tp here Notch --force --silent",
            "tp here Notch --silent --force",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    @Test
    fun `root player shorthand is accepted without overriding reserved subcommands`() {
        val root = dispatcher.parse("tp", source)
        assertTrue(root.exceptions.isEmpty())
        assertFalse(root.reader.canRead())

        val shorthand = dispatcher.parse("tp Notch", source)
        assertTrue(shorthand.exceptions.isEmpty())
        assertFalse(shorthand.reader.canRead())

        val canonical = dispatcher.parse("tp to Notch", source)
        assertTrue(canonical.exceptions.isEmpty())
        assertFalse(canonical.reader.canRead())

        val reserved = dispatcher.parse("tp accept Notch", source)
        assertTrue(reserved.exceptions.isEmpty())
        assertFalse(reserved.reader.canRead())
        val reservedNodes = reserved.context.nodes.map { it.node.name }
        assertTrue("accept" in reservedNodes)
        assertFalse("legacyPlayer" in reservedNodes)

        val invalidFlag = dispatcher.parse("tp to Notch --unknown", source)
        assertTrue(invalidFlag.reader.canRead() || invalidFlag.exceptions.isNotEmpty())
    }

    @Test
    fun `random teleport command and legacy alias both parse`() {
        listOf("tp random", "rtp").forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    @Test
    fun `subcommands player names and valid options are suggested`() {
        val rootSuggestions =
            dispatcher.getCompletionSuggestions(
                dispatcher.parse("tp ", source),
            ).get(1, TimeUnit.SECONDS).list.map { it.text }
        assertTrue(rootSuggestions.containsAll(listOf("to", "here", "send", "all", "pos", "random", "back")))

        val playerSuggestions =
            dispatcher.getCompletionSuggestions(
                dispatcher.parse("tp to ", source),
            ).get(1, TimeUnit.SECONDS).list.map { it.text }
        assertTrue(playerSuggestions.contains("Notch"))

        val optionSuggestions =
            dispatcher.getCompletionSuggestions(
                dispatcher.parse("tp to Notch --", source),
            ).get(1, TimeUnit.SECONDS).list.map { it.text }
        assertTrue(optionSuggestions.containsAll(listOf("--force", "--silent")))
    }
}
