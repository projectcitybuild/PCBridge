package com.projectcitybuild.pcbridge.features.announcements.listeners

import com.projectcitybuild.pcbridge.features.announcements.actions.StartAnnouncementTimer
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class AnnouncementEnableListenerTest {
    private val timer = mock(StartAnnouncementTimer::class.java)
    private val plugin = mock(JavaPlugin::class.java)
    private val listener = AnnouncementEnableListener(timer, plugin)

    @Test
    fun `starts and stops announcements for this plugin`() {
        val enabled = mock(PluginEnableEvent::class.java)
        val disabled = mock(PluginDisableEvent::class.java)
        whenever(enabled.plugin).thenReturn(plugin)
        whenever(disabled.plugin).thenReturn(plugin)

        listener.onPluginEnable(enabled)
        verify(timer).start()

        listener.onPluginDisable(disabled)
        verify(timer).stop()
    }

    @Test
    fun `ignores lifecycle events for other plugins`() {
        val enabled = mock(PluginEnableEvent::class.java)
        val disabled = mock(PluginDisableEvent::class.java)
        whenever(enabled.plugin).thenReturn(mock(JavaPlugin::class.java))
        whenever(disabled.plugin).thenReturn(mock(JavaPlugin::class.java))

        listener.onPluginEnable(enabled)
        verify(timer, never()).start()

        listener.onPluginDisable(disabled)
        verify(timer, never()).stop()
    }
}
