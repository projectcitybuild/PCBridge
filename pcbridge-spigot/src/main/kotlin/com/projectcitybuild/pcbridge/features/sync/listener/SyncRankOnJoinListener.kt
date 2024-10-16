package com.projectcitybuild.pcbridge.features.sync.listener

import com.projectcitybuild.pcbridge.features.playerstate.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.features.sync.actions.SyncPlayerGroups
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncRankOnJoinListener(
    private val syncPlayerGroups: SyncPlayerGroups,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        syncPlayerGroups.execute(
            playerUUID = event.playerUUID,
            groups = event.playerData.account?.groups ?: emptyList(),
            donationPerks = event.playerData.donationPerks,
        )
    }
}
