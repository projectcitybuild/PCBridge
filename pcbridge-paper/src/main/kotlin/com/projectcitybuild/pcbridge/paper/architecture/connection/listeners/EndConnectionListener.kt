package com.projectcitybuild.pcbridge.paper.architecture.connection.listeners

import com.projectcitybuild.pcbridge.paper.architecture.connection.connectionTracer
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.sync.domain.repositories.ConnectionRepository
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.uuid
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class EndConnectionListener(
    private val connectionRepository: ConnectionRepository,
    private val session: SessionStore,
    private val time: LocalizedTime,
    private val errorTracker: ErrorTracker,
) : Listener {
    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true,
    )
    suspend fun onPlayerQuit(
        event: PlayerQuitEvent,
    ) = event.scoped(connectionTracer, this::class.java) {
        runCatching {
            val uuid = event.player.uniqueId
            val state = session.state.players[uuid]
                ?: throw Exception("Missing player state for $uuid")

            connectionRepository.end(
                uuid = uuid,
                sessionSeconds = state.sessionSeconds(time),
            )
        }.onFailure {
            log.error(it) { "An error occurred while authorizing a connection" }
            errorTracker.report(it)
        }
    }
}