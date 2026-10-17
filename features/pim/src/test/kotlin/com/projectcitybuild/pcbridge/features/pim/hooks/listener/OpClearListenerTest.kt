package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class OpClearListenerTest {
    @Test
    fun `clears operator status after this plugin is enabled`() {
        val plugin = mock(JavaPlugin::class.java)
        val server = mock(Server::class.java)
        val operator = mock(Player::class.java)
        whenever(server.operators).thenReturn(setOf(operator))
        val event = mock(PluginEnableEvent::class.java)
        whenever(event.plugin).thenReturn(plugin)

        OpClearListener(plugin, server).onPluginEnabled(event)

        verify(operator).isOp = false
    }

    @Test
    fun `does not clear operator status for another plugin enable event`() {
        val plugin = mock(JavaPlugin::class.java)
        val server = mock(Server::class.java)
        val event = mock(PluginEnableEvent::class.java)
        whenever(event.plugin).thenReturn(mock(JavaPlugin::class.java))

        OpClearListener(plugin, server).onPluginEnabled(event)

        verify(server, never()).operators
    }
}
