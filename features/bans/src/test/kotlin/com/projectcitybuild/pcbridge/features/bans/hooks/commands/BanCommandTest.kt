package com.projectcitybuild.pcbridge.features.bans.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BanCommandTest {
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val dialog = mock(DialogLike::class.java)
    private var builtFor: String? = null

    @Test
    fun `opens a dialog using the canonical name of an online target`() =
        runTest {
            val target = mock(Player::class.java)
            whenever(sender.hasPermission(org.mockito.kotlin.any<String>())).thenReturn(true)
            whenever(server.onlinePlayers).thenReturn(setOf(target))
            whenever(target.name).thenReturn("Steve")

            val command =
                BanCommand(
                    plugin = mock(Plugin::class.java),
                    server = server,
                    createDialog = { name: String? ->
                        builtFor = name
                        dialog
                    },
                    taskExecutor = CoroutineScopeCommandTaskExecutor(this),
                )
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            dispatcher.execute("ban Steve", source())
            runCurrent()
            advanceUntilIdle()

            assert(builtFor == "Steve")
            verify(sender).showDialog(dialog)
        }

    private fun source(): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
