package com.projectcitybuild.spigot.stores

import com.projectcitybuild.core.protocols.PlayerStoreWrapper
import com.projectcitybuild.core.services.PlayerStore
import org.bukkit.entity.Player as BukkitPlayer
import com.projectcitybuild.entities.models.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPlayerStore(val plugin: WeakReference<JavaPlugin>) : PlayerStoreWrapper, Listener {
    override val store: PlayerStore = PlayerStore()

    init {
        val plugin = plugin.get()
        if (plugin != null) {
            plugin.server?.pluginManager?.registerEvents(this, plugin)
            rebuildStore()
        }
    }

    @EventHandler
    fun playerJoined(event: PlayerJoinEvent) {
        val player = event.player.makeModel()
        store.put(event.player.uniqueId, player)
    }

    @EventHandler
    fun playerLeft(event: PlayerQuitEvent) {
        store.remove(event.player.uniqueId)
    }

    private fun rebuildStore() {
        store.clear()

        plugin.get()?.server?.onlinePlayers?.forEach { onlinePlayer ->
            val player = onlinePlayer.makeModel()
            store.put(onlinePlayer.uniqueId, player)
        }
    }

    private fun BukkitPlayer.makeModel() : Player {
        val model = Player(uuid = this.uniqueId, isMuted = false)

        return model
    }
}