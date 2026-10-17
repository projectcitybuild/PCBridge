package com.projectcitybuild.pcbridge.features.config.hooks.commands

import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class ConfigCommandTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val sender = mock(CommandSender::class.java)
    private val command = ConfigCommand(mock(Plugin::class.java), remoteConfig)

    @Test
    fun `fetches remote config and reports completion`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(context.input).thenReturn("/config reload")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)

            command.execute(context)

            verify(sender).sendRichMessage("<gray>Fetching config...</gray>")
            verify(remoteConfig).fetch()
            verify(sender).sendRichMessage("<green>Remote config reloaded</green>")
        }
}
