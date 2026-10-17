package com.projectcitybuild.pcbridge.features.sync.hooks.commands

import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class SyncCommandTest {
    private val context = mock(PaperCommandContext::class.java)
    private val source = mock(CommandSourceStack::class.java)
    private val sender = mock(CommandSender::class.java)
    private val player = mock(Player::class.java)
    private val syncPlayer = mock(SyncPlayer::class.java)
    private val command = SyncCommand(mock(Plugin::class.java), syncPlayer)
    private val playerId = UUID.randomUUID()

    @Test
    fun `syncs the command sender when they are a player`() =
        runTest {
            whenever(context.input).thenReturn("/sync")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(player)
            whenever(player.uniqueId).thenReturn(playerId)

            command.syncSelf(context)

            verify(player).sendRichMessage("<gray>Fetching player data...</gray>")
            verify(syncPlayer).execute(playerUUID = playerId)
        }

    @Test
    fun `syncs a selected player and reports completion to sender`() =
        runTest {
            whenever(context.input).thenReturn("/sync Steve")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)
            whenever(context.getArgument("player", Player::class.java)).thenReturn(player)
            whenever(player.uniqueId).thenReturn(playerId)
            whenever(player.name).thenReturn("Steve")

            command.syncOther(context)

            verify(sender).sendRichMessage("<gray>Fetching player data for Steve...</gray>")
            verify(syncPlayer).execute(playerUUID = playerId)
            verify(sender).sendRichMessage("<green>Player data synced</green>")
        }

    @Test
    fun `uses self sync behavior when a player selects themselves`() =
        runTest {
            whenever(context.input).thenReturn("/sync Alex")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(player)
            whenever(context.getArgument("player", Player::class.java)).thenReturn(player)
            whenever(player.uniqueId).thenReturn(playerId)

            command.syncOther(context)

            verify(player).sendRichMessage("<gray>Fetching player data...</gray>")
            verify(syncPlayer).execute(playerUUID = playerId)
        }
}
