package com.projectcitybuild.pcbridge.features.bans.hooks.commands

import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class BanCommandTest {
    private val context = mock(PaperCommandContext::class.java)
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val dialog = mock(DialogLike::class.java)
    private var builtFor: String? = null
    private val command =
        BanCommand(mock(Plugin::class.java), server) {
            builtFor = it
            dialog
        }

    @Test
    fun `opens a dialog using the canonical name of an online target`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            val target = mock(Player::class.java)
            whenever(context.input).thenReturn("/ban Steve")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)
            whenever(server.onlinePlayers).thenReturn(setOf(target))
            whenever(target.name).thenReturn("Steve")
            whenever(context.getArgument("player", String::class.java)).thenReturn("Steve")

            command.execute(context)

            assert(builtFor == "Steve")
            verify(sender).showDialog(dialog)
        }
}
