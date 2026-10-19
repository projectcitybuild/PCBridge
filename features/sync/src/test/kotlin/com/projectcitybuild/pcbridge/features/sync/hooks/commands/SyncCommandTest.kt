package com.projectcitybuild.pcbridge.features.sync.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SyncCommandTest {
    private val server = mock(Server::class.java)
    private val plugin = mock(Plugin::class.java)
    private val player = mock(Player::class.java)
    private val syncPlayer = mock(SyncPlayer::class.java)
    private val playerId = UUID.randomUUID()

    @Test
    fun `syncs the command sender when they are a player`() =
        runTest {
            whenever(plugin.server).thenReturn(server)
            whenever(player.uniqueId).thenReturn(playerId)
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("sync", source(player))
            runCurrent()
            advanceUntilIdle()

            verify(player).sendMessage(l10n.sync.syncFetchingPlayerData)
            verify(syncPlayer).execute(playerUUID = playerId)
        }

    @Test
    fun `syncs a selected player and reports completion to sender`() =
        runTest {
            val sender = mock(CommandSender::class.java)
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(plugin.server).thenReturn(server)
            whenever(server.onlinePlayers).thenReturn(setOf(player))
            whenever(player.uniqueId).thenReturn(playerId)
            whenever(player.name).thenReturn("Steve")
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("sync Steve", source(sender))
            runCurrent()
            advanceUntilIdle()

            verify(sender).sendMessage(l10n.sync.syncFetchingPlayerDataFor("Steve"))
            verify(syncPlayer).execute(playerUUID = playerId)
            verify(sender).sendMessage(l10n.sync.syncPlayerDataSynced)
        }

    @Test
    fun `uses self sync behavior when a player selects themselves`() =
        runTest {
            whenever(plugin.server).thenReturn(server)
            whenever(server.onlinePlayers).thenReturn(setOf(player))
            whenever(player.uniqueId).thenReturn(playerId)
            whenever(player.name).thenReturn("Alex")
            whenever(player.hasPermission(any<String>())).thenReturn(true)
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("sync Alex", source(player))
            runCurrent()
            advanceUntilIdle()

            verify(player).sendMessage(l10n.sync.syncFetchingPlayerData)
            verify(syncPlayer).execute(playerUUID = playerId)
        }

    private fun dispatcher(executor: CoroutineScopeCommandTaskExecutor): CommandDispatcher<CommandSourceStack> {
        val command = SyncCommand(plugin, syncPlayer, executor)
        return CommandDispatcher<CommandSourceStack>().also { it.root.addChild(command.literal()) }
    }

    private fun source(sender: CommandSender): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
