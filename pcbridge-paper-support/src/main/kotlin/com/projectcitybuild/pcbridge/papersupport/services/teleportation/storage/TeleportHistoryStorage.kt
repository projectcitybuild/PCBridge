package com.projectcitybuild.pcbridge.papersupport.services.teleportation.storage

import com.projectcitybuild.pcbridge.papersupport.services.teleportation.events.PlayerPreTeleportEvent
import com.projectcitybuild.pcbridge.papersupport.support.spigot.SpigotEventBroadcaster
import org.bukkit.Location
import org.bukkit.entity.Player

class TeleportHistoryStorage(
    private val eventBroadcaster: SpigotEventBroadcaster,
) {
    suspend fun put(prev: Location, next: Location, player: Player) {
        // For now, we broadcast an event so that Essentials can save the location.
        // Later we'll store this in our backend instead
        eventBroadcaster.broadcast(
            PlayerPreTeleportEvent(player)
        )
    }
}