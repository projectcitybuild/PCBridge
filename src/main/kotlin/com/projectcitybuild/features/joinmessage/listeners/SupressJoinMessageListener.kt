package com.projectcitybuild.features.joinmessage.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class SupressJoinMessageListener: Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Supress join messages on Spigot servers because only the
        // Bungeecord server knows when a player joins the network
        event.joinMessage = null
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        // Supress quit messages on Spigot servers because only the
        // Bungeecord server knows when a player leaves the network
        event.quitMessage = null
    }
}