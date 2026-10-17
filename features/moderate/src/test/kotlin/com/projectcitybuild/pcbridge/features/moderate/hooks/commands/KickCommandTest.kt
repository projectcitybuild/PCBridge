package com.projectcitybuild.pcbridge.features.moderate.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class KickCommandTest {
    private val target = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val server = mock(Server::class.java)
    private val command = KickCommand(mock(JavaPlugin::class.java), server)

    @Test
    fun `kicks with the provided reason and broadcasts it`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(context.input).thenReturn("/kick Steve Griefing")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(mock(org.bukkit.command.CommandSender::class.java))
            whenever(context.getArgument("player", Player::class.java)).thenReturn(target)
            whenever(context.getArgument("reason", String::class.java)).thenReturn("Griefing")
            whenever(target.name).thenReturn("Steve")

            command.execute(context)

            verify(target).kick(net.kyori.adventure.text.Component.text("Griefing"), PlayerKickEvent.Cause.KICK_COMMAND)
            verify(server).broadcast(l10n.playerWasKickedForReason("Steve", "Griefing"))
        }

    @Test
    fun `uses default reason if none was supplied`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(context.input).thenReturn("/kick Steve")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(mock(org.bukkit.command.CommandSender::class.java))
            whenever(context.getArgument("player", Player::class.java)).thenReturn(target)
            whenever(context.getArgument("reason", String::class.java)).thenReturn(null)
            whenever(target.name).thenReturn("Steve")

            command.execute(context)

            verify(target).kick(l10n.kickedFromServer, PlayerKickEvent.Cause.KICK_COMMAND)
            verify(server).broadcast(l10n.playerWasKicked("Steve"))
        }
}
