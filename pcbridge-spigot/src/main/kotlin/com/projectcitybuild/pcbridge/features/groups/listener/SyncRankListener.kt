package com.projectcitybuild.pcbridge.features.groups.listener

import com.projectcitybuild.pcbridge.features.playerstate.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.features.groups.actions.SyncPlayerGroups
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncRankListener(
    private val syncPlayerGroups: SyncPlayerGroups,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        syncPlayerGroups.execute(
            playerUUID = event.playerUUID,
            groups = event.state.account?.groups ?: emptyList(),
            donationPerks = event.state.donationPerks,
        )
    }
}
