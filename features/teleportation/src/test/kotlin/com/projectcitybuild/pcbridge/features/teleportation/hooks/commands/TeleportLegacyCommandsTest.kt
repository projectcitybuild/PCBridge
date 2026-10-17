package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TeleportLegacyCommandsTest {
    private val player =
        mock<Player>().also {
            whenever(it.name).thenReturn("Notch")
        }

    private val server =
        mock<Server>().also {
            whenever(it.onlinePlayers).thenReturn(listOf(player))
        }

    private val source = mock<CommandSourceStack>()

    private val sender =
        mock<CommandSender>().also {
            whenever(it.hasPermission(any<String>())).thenReturn(true)
        }

    private val plugin = mock<Plugin>()

    private val application = mock<TeleportApplication>()
    private val random = TeleportRandomCommand(plugin, application)

    private val commands =
        TeleportLegacyCommands(
            plugin = plugin,
            server = server,
            application = application,
        ).commands()

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
                    position = TeleportPosCommand(plugin, application),
                    random = random,
                    back = TeleportBackCommand(plugin, server, application),
                    accept = TeleportAcceptCommand(plugin, server, application),
                    deny = TeleportDenyCommand(plugin, server, application),
                    cancel = TeleportCancelCommand(plugin, application),
                ).literal(),
            )
            commands.forEach { root.addChild(it.literal()) }
        }

    @Test
    fun `all supported Essentials teleport names are independently registered roots`() {
        val registered = commands.map { it.literal().name }.toSet()

        assertTrue(
            registered.containsAll(
                setOf(
                    "tpa",
                    "tpahere",
                    "tpaccept",
                    "tpyes",
                    "tpdeny",
                    "tpno",
                    "tpcancel",
                    "tpacancel",
                    "tpo",
                    "tpohere",
                    "tphere",
                    "tpall",
                    "tppos",
                    "back",
                    "rtp",
                ),
            ),
        )
        assertEquals(registered.size, commands.size)
    }

    @Test
    fun `legacy roots use the canonical PCBridge permissions`() {
        whenever(sender.hasPermission(any<String>())).thenReturn(false)
        whenever(sender.hasPermission(PermissionNode.TELEPORT_REQUEST.node)).thenReturn(true)

        val tpaPlayer = commands.single { it.literal().name == "tpa" }.literal().getChild("player")
        val tpPlayer = dispatcher.root.getChild("tp")?.getChild("player")
        val forcePlayer = commands.single { it.literal().name == "tpo" }.literal().getChild("player")
        val randomAlias = commands.single { it.literal().name == "rtp" }.literal()
        val randomSubcommand = dispatcher.root.getChild("tp")?.getChild("random")

        assertTrue(tpaPlayer.canUse(source))
        assertTrue(tpPlayer?.canUse(source) == true)
        assertFalse(forcePlayer.canUse(source))

        whenever(sender.hasPermission(PermissionNode.TELEPORT_FORCE.node)).thenReturn(true)
        assertTrue(forcePlayer.canUse(source))

        assertFalse(randomAlias.canUse(source))
        assertFalse(randomSubcommand?.canUse(source) == true)
        whenever(sender.hasPermission(PermissionNode.TELEPORT_RANDOM.node)).thenReturn(true)
        assertTrue(randomAlias.canUse(source))
        assertTrue(randomSubcommand?.canUse(source) == true)
    }

    @Test
    fun `legacy command argument forms match their canonical operations`() {
        listOf(
            "tpa Notch",
            "tpahere Notch",
            "tpaccept",
            "tpaccept Notch",
            "tpdeny",
            "tpdeny Notch",
            "tpcancel",
            "tpacancel Notch",
            "tpo Notch",
            "tpohere Notch",
            "tphere Notch",
            "tpall",
            "tpall Notch",
            "tppos 1 64 -2",
            "tppos 1 64 -2 90",
            "tppos 1 64 -2 90 45",
            "back",
            "back Notch",
            "rtp",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    @Test
    fun `legacy tpall does not accept force or silent flags`() {
        listOf(
            "tpall --force",
            "tpall --silent",
            "tpall -f",
            "tpall -s",
            "tpall Notch --force",
            "tpall Notch --silent",
            "tpall Notch -f",
            "tpall Notch -s",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)
            assertTrue(
                parse.exceptions.isNotEmpty() || parse.reader.canRead(),
                "Unexpectedly accepted legacy option: $input",
            )
        }
    }

    @Test
    fun `legacy tppos does not accept a world argument`() {
        val parse = dispatcher.parse("tppos 1 64 -2 world", source)
        assertTrue(
            parse.exceptions.isNotEmpty() || parse.reader.canRead(),
            "Unexpectedly accepted a world argument",
        )
    }
}
