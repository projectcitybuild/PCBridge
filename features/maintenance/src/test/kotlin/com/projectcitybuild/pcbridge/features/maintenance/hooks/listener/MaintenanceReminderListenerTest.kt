package com.projectcitybuild.pcbridge.features.maintenance.hooks.listener

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceState
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MaintenanceReminderListenerTest {
    private val server = mock(Server::class.java)
    private val scheduler = TestCoroutineScheduler()
    private val timer =
        SpigotTimer(
            mock(JavaPlugin::class.java),
            StandardTestDispatcher(scheduler),
            mock(ErrorTracker::class.java),
        )
    private val store = mock(Store::class.java)
    private val plugin = mock(JavaPlugin::class.java)
    private val listener = MaintenanceReminderListener(plugin, store, server, timer)

    @Test
    fun `sends reminders while enabled and stops after it is disabled`() {
        listener.onMaintenanceToggled(MaintenanceToggledEvent(true))
        scheduler.advanceTimeBy(5.minutes.inWholeMilliseconds)
        scheduler.runCurrent()
        verify(server).broadcast(org.mockito.kotlin.any<net.kyori.adventure.text.Component>())

        listener.onMaintenanceToggled(MaintenanceToggledEvent(false))
        scheduler.advanceTimeBy(5.minutes.inWholeMilliseconds)
        scheduler.runCurrent()

        verify(server).broadcast(org.mockito.kotlin.any<net.kyori.adventure.text.Component>())
        timer.shutdown()
    }

    @Test
    fun `ignores plugin enable events for other plugins`() {
        val ignored = mock(org.bukkit.event.server.PluginEnableEvent::class.java)
        whenever(ignored.plugin).thenReturn(mock(JavaPlugin::class.java))
        listener.onPluginEnable(ignored)
        val ownPluginEvent = mock(org.bukkit.event.server.PluginEnableEvent::class.java)
        whenever(ownPluginEvent.plugin).thenReturn(plugin)
        whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState(enabled = true))
        listener.onPluginEnable(ownPluginEvent)
        scheduler.advanceTimeBy(5.minutes.inWholeMilliseconds)
        scheduler.runCurrent()
        verify(server).broadcast(org.mockito.kotlin.any<net.kyori.adventure.text.Component>())
        val disable = mock(org.bukkit.event.server.PluginDisableEvent::class.java)
        whenever(disable.plugin).thenReturn(plugin)
        listener.onPluginDisable(disable)
        scheduler.advanceTimeBy(5.minutes.inWholeMilliseconds)
        scheduler.runCurrent()
        verify(server).broadcast(org.mockito.kotlin.any<net.kyori.adventure.text.Component>())
        timer.shutdown()
    }
}
