package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

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
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExtinguishCommandTest {
    private val target = mock(Player::class.java)
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)

    @Test
    fun `extinguishes a target who is on fire`() = runTest {
        whenever(sender.hasPermission(any<String>())).thenReturn(true)
        whenever(server.onlinePlayers).thenReturn(setOf(target))
        whenever(target.name).thenReturn("Alex")
        whenever(target.fireTicks).thenReturn(20)
        val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

        dispatcher.execute("extinguish Alex", source())
        runCurrent()
        advanceUntilIdle()

        verify(target).fireTicks = 0
        verify(sender).sendMessage(l10n.gameplay.playerExtinguished("Alex"))
    }

    @Test
    fun `reports when the target is not on fire`() = runTest {
        whenever(sender.hasPermission(any<String>())).thenReturn(true)
        whenever(server.onlinePlayers).thenReturn(setOf(target))
        whenever(target.name).thenReturn("Alex")
        whenever(target.fireTicks).thenReturn(0)
        val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

        dispatcher.execute("extinguish Alex", source())
        runCurrent()
        advanceUntilIdle()

        verify(target, never()).setFireTicks(any())
        verify(sender).sendMessage(l10n.gameplay.playerNotOnFire("Alex"))
    }

    private fun dispatcher(executor: CoroutineScopeCommandTaskExecutor): CommandDispatcher<CommandSourceStack> {
        val command = ExtinguishCommand(mock(Plugin::class.java), server, executor)
        return CommandDispatcher<CommandSourceStack>().also { it.root.addChild(command.literal()) }
    }

    private fun source(): CommandSourceStack = mock(CommandSourceStack::class.java).also { source ->
        whenever(source.sender).thenReturn(sender)
    }
}
