package com.projectcitybuild.pcbridge.features.config.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ConfigCommandTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val sender = mock(CommandSender::class.java)

    @Test
    fun `fetches remote config and reports completion`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            val command =
                ConfigCommand(mock(Plugin::class.java), remoteConfig, CoroutineScopeCommandTaskExecutor(this))
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            dispatcher.execute("config reload", source())
            runCurrent()
            advanceUntilIdle()

            verify(sender).sendMessage(l10n.config.configFetching)
            verify(remoteConfig).fetch()
            verify(sender).sendMessage(l10n.config.configReloaded)
        }

    @Test
    fun `does not reload config without permission`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(false)
            val command =
                ConfigCommand(mock(Plugin::class.java), remoteConfig, CoroutineScopeCommandTaskExecutor(this))
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            assertThrows<CommandSyntaxException> {
                dispatcher.execute("config reload", source())
            }
            runCurrent()
            advanceUntilIdle()

            verify(remoteConfig, never()).fetch()
        }

    private fun source(): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
