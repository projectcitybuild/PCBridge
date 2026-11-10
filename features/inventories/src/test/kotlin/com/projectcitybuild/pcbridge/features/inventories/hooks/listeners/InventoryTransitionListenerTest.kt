package com.projectcitybuild.pcbridge.features.inventories.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.inventories.domain.services.InventoryTransitionService
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.logging.Logger

class InventoryTransitionListenerTest {
    @Test
    fun `notifies player when world inventory transition fails`() {
        val plugin = mock<JavaPlugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("inventory-transition-test"))
        val player = mock<Player>()
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
