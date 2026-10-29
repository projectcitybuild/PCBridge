package com.projectcitybuild.pcbridge.runtime.connection.listeners

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.runtime.connection.connectionTracer
import com.projectcitybuild.pcbridge.runtime.listeners.runListenerSafely
import com.projectcitybuild.pcbridge.runtime.listeners.traced
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
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
    suspend fun onPlayerQuit(event: PlayerQuitEvent) =
        event.runListenerSafely(
            errorTracker,
            "An error occurred while ending a connection",
        ) {
            event.traced(connectionTracer, this::class.java) {
                val uuid = event.player.uniqueId
                val state =
                    session.state.players[uuid]
                        ?: throw Exception("Missing player state for $uuid")

                connectionRepository.end(
                    uuid = uuid,
                    sessionSeconds = state.sessionSeconds(time),
                )
            }
        }
}
