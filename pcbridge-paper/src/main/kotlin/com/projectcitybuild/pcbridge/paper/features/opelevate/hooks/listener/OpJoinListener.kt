package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.http.pcb.models.OpElevation
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
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
    private val session: SessionStore,
    private val opElevationService: OpElevationService,
): Listener {
    @EventHandler
    fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) = event.scopedSync(opElevateTracer, this::class.java) {
        val playerState = session.state.players[event.player.uniqueId]
            ?: run {
                logSync.warn { "Cannot determine op state: player state missing on join event" }
                event.player.isOp = false
                return@scopedSync
            }

        opElevationService.handleJoin(
            player = event.player,
            elevation = playerState.synced.elevation(),
        )
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        opElevationService.revokeSilently(event.player.uniqueId)
    }

    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        // PluginEnableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) return

        event.scopedSync(opElevateTracer, this::class.java) {
            server.operators.forEach { it.isOp = false }
        }
    }
}

private fun PlayerSyncedState.elevation(): OpElevation?
    = (this as? PlayerSyncedState.Valid)?.opElevation