package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.runBlocking
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TeleportLegacyCommandsTest {
    private val player = mock<Player>().also {
        whenever(it.name).thenReturn("Notch")
    }
    private val target = mock<Player>().also {
        whenever(it.name).thenReturn("Alex")
    }

    private val server = mock<Server>().also {
        whenever(it.onlinePlayers).thenReturn(listOf(player, target))
        whenever(it.isPrimaryThread).thenReturn(true)
        whenever(it.pluginManager).thenReturn(mock<PluginManager>())
    }

    private val source = mock<CommandSourceStack>()

    private val operator = mock<Player>().also {
        whenever(it.name).thenReturn("Operator")
        whenever(it.hasPermission(any<String>())).thenReturn(true)
    }

    private val plugin = mock<Plugin>().also {
        whenever(it.server).thenReturn(server)
        whenever(it.isEnabled).thenReturn(true)
    }

    private val application = mock<TeleportService>()
    private val random = TeleportRandomCommand(plugin, application)

    private val commands = TeleportLegacyCommands(
        plugin = plugin,
        server = server,
        teleportService = application,
    ).commands()

    private val dispatcher = CommandDispatcher<CommandSourceStack>().apply {
        whenever(source.sender).thenReturn(operator)
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
                    "tp",
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
    fun `legacy roots use their respective permissions`() {
        whenever(operator.hasPermission(any<String>())).thenReturn(false)
        whenever(operator.hasPermission(PermissionNode.TELEPORT_REQUEST.node)).thenReturn(true)

        val tpaherePlayer = commands.single { it.literal().name == "tpahere" }.literal().getChild("player")
        val tpPlayer = dispatcher.root.getChild("tp")?.getChild("player")
        val forcePlayer = commands.single { it.literal().name == "tpo" }.literal().getChild("player")
        val tpoherePlayer = commands.single { it.literal().name == "tpohere" }.literal().getChild("player")
        val tpherePlayer = commands.single { it.literal().name == "tphere" }.literal().getChild("player")
        val randomAlias = commands.single { it.literal().name == "rtp" }.literal()
        val randomSubcommand = dispatcher.root.getChild("tp")?.getChild("random")

        assertTrue(tpPlayer?.canUse(source) == true)
        assertFalse(forcePlayer.canUse(source))
        assertFalse(tpaherePlayer.canUse(source))
        assertFalse(tpoherePlayer.canUse(source))
        assertFalse(tpherePlayer.canUse(source))

        whenever(operator.hasPermission("essentials.tpahere")).thenReturn(true)
        assertTrue(tpaherePlayer.canUse(source))
        whenever(operator.hasPermission("essentials.tphere")).thenReturn(true)
        assertTrue(tpherePlayer.canUse(source))
        assertFalse(tpoherePlayer.canUse(source))

        whenever(operator.hasPermission("essentials.tpohere")).thenReturn(true)
        assertTrue(tpoherePlayer.canUse(source))
        whenever(operator.hasPermission("essentials.tpo")).thenReturn(true)
        assertTrue(forcePlayer.canUse(source))

        assertFalse(randomAlias.canUse(source))
        assertFalse(randomSubcommand?.canUse(source) == true)
        whenever(operator.hasPermission(PermissionNode.TELEPORT_RANDOM.node)).thenReturn(true)
        assertTrue(randomAlias.canUse(source))
        assertTrue(randomSubcommand?.canUse(source) == true)
    }

    @Test
    fun `tp shorthand and to execute the normal teleport operation`() {
        executeCommand("tp Notch")
        executeCommand("tp to Notch")

        runBlocking {
            verify(application, times(2)).to(operator, player, TeleportFlags())
        }
    }

    @Test
    fun `here and send execute with default flags`() {
        executeCommand("tp here Notch")
        executeCommand("tp send Notch Alex")

        runBlocking {
            verify(application).here(operator, player, TeleportFlags())
            verify(application).send(operator, player, target, TeleportFlags())
        }
    }

    @Test
    fun `force and silent options are passed to each teleport operation`() {
        val flags = TeleportFlags(force = true, silent = true)

        executeCommand("tp to Notch --force --silent")
        executeCommand("tp here Notch --force --silent")
        executeCommand("tp send Notch Alex --force --silent")

        runBlocking {
            verify(application).to(operator, player, flags)
            verify(application).here(operator, player, flags)
            verify(application).send(operator, player, target, flags)
        }
    }

    @Test
    fun `legacy here aliases execute normal and forced operations`() {
        val forced = TeleportFlags(force = true)

        runBlocking {
            executeCommand("tphere Notch")
            executeCommand("tpohere Notch")
            verify(application, times(2)).here(operator, player, forced)

            executeCommand("tpall")
            verify(application).all(operator, null, forced)

            executeCommand("tpall Notch")
            verify(application).all(operator, player, forced)
        }
    }

    @Test
    fun `legacy forced operations are denied without force permission`() {
        whenever(operator.hasPermission(PermissionNode.TELEPORT_FORCE.node)).thenReturn(false)
        whenever(operator.hasPermission("essentials.tpohere")).thenReturn(false)

        listOf("tpohere Notch", "tpall", "tpall Notch").forEach { input ->
            assertThrows(CommandSyntaxException::class.java) {
                executeCommand(input)
            }
        }

        verifyNoInteractions(application)
    }

    @Test
    fun `legacy command argument forms match their canonical operations`() {
        listOf(
            "tp Notch",
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

    private fun executeCommand(input: String) {
        dispatcher.execute(dispatcher.parse(input, source))
    }
}
