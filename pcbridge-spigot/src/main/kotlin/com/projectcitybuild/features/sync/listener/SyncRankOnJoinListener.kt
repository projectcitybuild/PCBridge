package com.projectcitybuild.features.sync.listener

import com.projectcitybuild.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.features.sync.actions.SyncPlayerGroups
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncRankOnJoinListener(
    private val syncPlayerGroups: SyncPlayerGroups,
) : Listener {
    @EventHandler
    fun onConnectionPermitted(event: ConnectionPermittedEvent) {
        syncPlayerGroups.execute(
            playerUUID = event.playerUUID,
            groups = event.aggregate.account?.groups ?: emptyList(),
            donationPerks = event.aggregate.donationPerks,
        )
    }
}