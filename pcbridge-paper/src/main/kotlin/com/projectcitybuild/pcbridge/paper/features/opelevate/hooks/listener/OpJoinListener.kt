package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import java.time.LocalDateTime

class OpJoinListener(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val session: SessionStore
): Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerState = session.state.players[player.uniqueId]
        if (playerState == null) {
            logSync.warn { "Could not deop player: player state missing on join event" }
            return
        }
        val wasOp = event.player.isOp
        val isOp = if (playerState.synced is PlayerSyncedState.Valid) {
            val elevation = playerState.synced.opElevation
            elevation?.endedAt?.isAfter(LocalDateTime.now()) ?: false
        } else {
            false
        }
        event.player.isOp = isOp

        if (wasOp && !isOp) {
            event.player.sendRichMessage("<red><i>Your OP elevation expired</i></red>")
        }
    }

    @EventHandler
    fun onPluginEnabled(
        event: PluginEnableEvent,
    ) {
        // PluginEnableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) {
            return
        }
        event.scopedSync(opElevateTracer, this::class.java) {
            server.operators.forEach { it.isOp = false }
        }
    }
}