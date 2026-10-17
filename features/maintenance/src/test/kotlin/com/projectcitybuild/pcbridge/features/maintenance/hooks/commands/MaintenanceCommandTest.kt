package com.projectcitybuild.pcbridge.features.maintenance.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceState
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MaintenanceCommandTest {
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val store = mock(Store::class.java)
    private val events = mock(SpigotEventBroadcaster::class.java)

    @Test
    fun `reports current maintenance status`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState(enabled = true))
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("maintenance", source())
            runCurrent()
            advanceUntilIdle()

            verify(sender).sendRichMessage("Maintenance mode is currently ON")
        }

    @Test
    fun `changes state broadcasts event and announces maintenance`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState())
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("maintenance on", source())
            runCurrent()
            advanceUntilIdle()

            verify(store).mutate(eq(maintenanceStateKey), any())
            verify(events).broadcast(any<MaintenanceToggledEvent>())
            verify(server).broadcast(any<net.kyori.adventure.text.Component>())
        }

    @Test
    fun `does not rebroadcast a state that is already active`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(store.state(maintenanceStateKey)).thenReturn(MaintenanceState(enabled = true))
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("maintenance on", source())
            runCurrent()
            advanceUntilIdle()

            verify(sender).sendRichMessage("<red>Maintenance mode is already ON</red>")
            verify(store, never()).mutate(eq(maintenanceStateKey), any())
            verify(events, never()).broadcast(any<MaintenanceToggledEvent>())
        }

    private fun dispatcher(executor: CoroutineScopeCommandTaskExecutor): CommandDispatcher<CommandSourceStack> {
        val command = MaintenanceCommand(mock(Plugin::class.java), server, store, events, executor)
        return CommandDispatcher<CommandSourceStack>().also { it.root.addChild(command.literal()) }
    }

    private fun source(): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
