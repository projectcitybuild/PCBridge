package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderFillService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldBorderFillListener(
    private val fillService: WorldBorderFillService,
) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        fillService.onPlayerQuit(event.player.uniqueId)
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        fillService.onWorldUnload(event.world.uid)
    }
}
