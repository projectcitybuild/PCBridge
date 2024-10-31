package com.projectcitybuild.pcbridge.paper.features.groups.listener

import com.projectcitybuild.pcbridge.paper.features.playerstate.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.groups.actions.SyncPlayerGroups
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncRankListener(
    private val syncPlayerGroups: SyncPlayerGroups,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        syncPlayerGroups.execute(
            playerUUID = event.playerUUID,
            groups = event.state.groups,
        )
    }
}
