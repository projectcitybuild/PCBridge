package com.projectcitybuild.pcbridge.features.maintenance.hooks.commands

import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceState
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class MaintenanceCommandTest {
    private val context = mock(PaperCommandContext::class.java)
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val store = mock(Store::class.java)
    private val events = mock(SpigotEventBroadcaster::class.java)
    private val command = MaintenanceCommand(mock(Plugin::class.java), server, store, events)

    @Test
    fun `reports current maintenance status`() =
        runTest {
            setSender()
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState(enabled = true))

            command.status(context)

            verify(sender).sendRichMessage("Maintenance mode is currently ON")
        }

    @Test
    fun `changes state broadcasts event and announces maintenance`() =
        runTest {
            setSender()
            whenever(context.getArgument("enabled", Boolean::class.java)).thenReturn(true)
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState())

            command.toggle(context)

            verify(store).mutate(eq(maintenanceStateKey), any())
            verify(events).broadcast(any<MaintenanceToggledEvent>())
            verify(server).broadcast(any<net.kyori.adventure.text.Component>())
        }

    @Test
    fun `does not rebroadcast a state that is already active`() =
        runTest {
            setSender()
            whenever(context.getArgument("enabled", Boolean::class.java)).thenReturn(true)
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState(enabled = true))

            command.toggle(context)

            verify(sender).sendRichMessage("<red>Maintenance mode is already ON</red>")
            verify(store, never()).mutate(eq(maintenanceStateKey), any())
            verify(events, never()).broadcast(any<MaintenanceToggledEvent>())
        }

    private fun setSender() {
        val source = mock(CommandSourceStack::class.java)
        whenever(context.input).thenReturn("/maintenance")
        whenever(context.source).thenReturn(source)
        whenever(source.sender).thenReturn(sender)
    }
}
