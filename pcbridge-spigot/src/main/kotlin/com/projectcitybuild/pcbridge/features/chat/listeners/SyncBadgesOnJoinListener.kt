package com.projectcitybuild.pcbridge.features.chat.listeners

import com.projectcitybuild.pcbridge.core.state.Store
import com.projectcitybuild.pcbridge.features.bans.events.ConnectionPermittedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncBadgesOnJoinListener(
    private val store: Store,
) : Listener {
    @EventHandler
    suspend fun onConnectionPermitted(event: ConnectionPermittedEvent) {
        store.mutate { state ->
            val player =
                state.players[event.playerUUID]!!.copy(
                    badges = event.aggregate.badges,
                )
            val players =
                state.players.apply {
                    put(event.playerUUID, player)
                }
            state.copy(players = players)
        }
    }
}
