package com.projectcitybuild.pcbridge.features.inventories.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.inventories.domain.services.InventoryTransitionService
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID
import java.util.logging.Logger

class InventoryTransitionListenerTest {
    @Test
    fun `join followed by world change reconciles the final world without saving join state`() {
        val plugin = mock<JavaPlugin>()
        val server = mock<Server>()
        val scheduler = mock<BukkitScheduler>()
        whenever(plugin.server).thenReturn(server)
        whenever(server.scheduler).thenReturn(scheduler)
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.isOnline).thenReturn(true)
        val destination = mock<World>()
        whenever(destination.name).thenReturn("destination")
        whenever(player.world).thenReturn(destination)
        val joinEvent = mock<PlayerJoinEvent>()
        whenever(joinEvent.player).thenReturn(player)
        val source = mock<World>()
        whenever(source.name).thenReturn("source")
        val transitionService = mock<InventoryTransitionService>()
        whenever(transitionService.applyPlayerDataOnJoin).thenReturn(true)
        val listener = InventoryTransitionListener(plugin, transitionService)

        listener.onPlayerJoin(joinEvent)
        listener.onPlayerChangedWorld(PlayerChangedWorldEvent(player, source))

        val scheduledTask = argumentCaptor<Runnable>()
        verify(scheduler).runTask(eq(plugin), scheduledTask.capture())
        scheduledTask.firstValue.run()

        verify(transitionService).onPlayerJoined(player)
        verify(transitionService, never()).onWorldChanged(player, "source", "destination")
    }

    @Test
    fun `join application disabled does not suppress a world transition`() {
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val destination = mock<World>()
        whenever(destination.name).thenReturn("destination")
        whenever(player.world).thenReturn(destination)
        val joinEvent = mock<PlayerJoinEvent>()
        whenever(joinEvent.player).thenReturn(player)
        val source = mock<World>()
        whenever(source.name).thenReturn("source")
        val transitionService = mock<InventoryTransitionService>()
        whenever(transitionService.applyPlayerDataOnJoin).thenReturn(false)
        val listener = InventoryTransitionListener(mock(), transitionService)

        listener.onPlayerJoin(joinEvent)
        listener.onPlayerChangedWorld(PlayerChangedWorldEvent(player, source))

        verify(transitionService).onWorldChanged(player, "source", "destination")
    }

    @Test
    fun `does not save or load profiles for a cancelled game-mode change`() {
        val plugin = mock<JavaPlugin>()
        val transitionService = mock<InventoryTransitionService>()
        val event = mock<PlayerGameModeChangeEvent>()
        whenever(event.isCancelled).thenReturn(true)

        InventoryTransitionListener(plugin, transitionService).onPlayerGameModeChange(event)

        verifyNoInteractions(transitionService)
    }

    @Test
    fun `notifies player when world inventory transition fails`() {
        val plugin = mock<JavaPlugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("inventory-transition-test"))
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        val source = mock<World>()
        whenever(source.name).thenReturn("source")
        val destination = mock<World>()
        whenever(destination.name).thenReturn("destination")
        whenever(player.world).thenReturn(destination)

        val transitionService = mock<InventoryTransitionService>()
        doThrow(IllegalArgumentException("Invalid stored item"))
            .whenever(transitionService)
            .onWorldChanged(player, "source", "destination")

        InventoryTransitionListener(plugin, transitionService)
            .onPlayerChangedWorld(PlayerChangedWorldEvent(player, source))

        verify(player).sendMessage(l10n.inventories.transitionFailed)
    }
}
