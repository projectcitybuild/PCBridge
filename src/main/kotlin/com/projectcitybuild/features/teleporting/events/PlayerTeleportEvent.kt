package com.projectcitybuild.features.teleporting.events

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class PlayerTeleportEvent(
    val player: Player,
    val destinationPlayer: Player,
): SpigotEvent() {

    private val handlers = HandlerList()

    val destination: Location
        get() = destinationPlayer.location

    override fun getHandlers(): HandlerList {
        return handlers
    }
}