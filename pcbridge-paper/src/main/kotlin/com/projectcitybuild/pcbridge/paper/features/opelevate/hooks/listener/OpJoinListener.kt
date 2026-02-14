package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class OpJoinListener(
    private val plugin: JavaPlugin,
    private val server: Server,
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

    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        if (event.plugin != plugin) return

        event.scopedSync(opElevateTracer, this::class.java) {
            server.operators.forEach { it.isOp = false }
        }
    }
}