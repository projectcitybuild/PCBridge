package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class OpRestoreListener(
    private val opElevationService: OpElevationService,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) =
        event.tracedSync(pimTracer, this::class.java) {
            opElevationService.handleJoin(event.player)
        }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        opElevationService.handleLeave(event.player.uniqueId)
    }
}
