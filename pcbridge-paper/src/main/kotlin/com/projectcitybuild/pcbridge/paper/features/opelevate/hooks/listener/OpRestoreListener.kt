package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class OpRestoreListener(
    private val opElevationService: OpElevationService,
): Listener {
    @EventHandler
    fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) = event.scopedSync(opElevateTracer, this::class.java) {
        opElevationService.handleJoin(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        opElevationService.handleLeave(event.player.uniqueId)
    }
}