package com.projectcitybuild.features.joinmessage.listeners

import com.projectcitybuild.core.SpigotListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import javax.inject.Inject

class SupressJoinMessageListener @Inject constructor(): SpigotListener {

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