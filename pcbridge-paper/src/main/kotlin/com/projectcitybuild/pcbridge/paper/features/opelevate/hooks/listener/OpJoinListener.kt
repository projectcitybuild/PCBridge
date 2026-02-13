package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
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
    fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) = event.scopedSync(opElevateTracer, this::class.java) {
        val playerState = session.state.players[event.player.uniqueId]
        if (playerState == null) {
            logSync.warn { "Cannot determine op state: player state missing on join event" }
            event.player.isOp = false
            return@scopedSync
        }
        val wasOp = event.player.isOp
        val isOp = playerState.synced.isOp()

        event.player.isOp = isOp

        if (wasOp && !isOp) {
            event.player.sendRichMessage(l10n.opElevationRevoked)
        }
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

private fun PlayerSyncedState.isOp(): Boolean {
    if (this !is PlayerSyncedState.Valid) return false
    if (opElevation == null) return false
    return opElevation.endedAt.isAfter(LocalDateTime.now())
}