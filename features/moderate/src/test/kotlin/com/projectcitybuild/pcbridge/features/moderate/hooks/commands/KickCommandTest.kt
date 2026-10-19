package com.projectcitybuild.pcbridge.features.moderate.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class KickCommandTest {
    private val target = mock(Player::class.java)
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val plugin = mock(JavaPlugin::class.java)

    @Test
    fun `kicks with the provided reason and broadcasts it`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(plugin.server).thenReturn(server)
            whenever(server.onlinePlayers).thenReturn(setOf(target))
            whenever(target.name).thenReturn("Steve")
            whenever(target.uniqueId).thenReturn(UUID.randomUUID())
            val command = KickCommand(plugin, server, CoroutineScopeCommandTaskExecutor(this))
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            dispatcher.execute("kick Steve Griefing", source())
            runCurrent()
            advanceUntilIdle()

            verify(target).kick(net.kyori.adventure.text.Component.text("Griefing"), PlayerKickEvent.Cause.KICK_COMMAND)
            verify(server).broadcast(l10n.moderation.playerWasKickedForReason("Steve", "Griefing"))
        }

    @Test
    fun `uses default reason if none was supplied`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(plugin.server).thenReturn(server)
            whenever(server.onlinePlayers).thenReturn(setOf(target))
            whenever(target.name).thenReturn("Steve")
            whenever(target.uniqueId).thenReturn(UUID.randomUUID())
            val command = KickCommand(plugin, server, CoroutineScopeCommandTaskExecutor(this))
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            dispatcher.execute("kick Steve", source())
            runCurrent()
            advanceUntilIdle()

            verify(target).kick(l10n.moderation.kickedFromServer, PlayerKickEvent.Cause.KICK_COMMAND)
            verify(server).broadcast(l10n.moderation.playerWasKicked("Steve"))
        }

    private fun source(): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
