package com.projectcitybuild.spigot.stores

import com.projectcitybuild.core.extensions.makeModel
import com.projectcitybuild.core.protocols.PlayerStoreWrapper
import com.projectcitybuild.core.services.PlayerStore
import com.projectcitybuild.entities.models.Player
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class SpigotPlayerStore(val plugin: WeakReference<JavaPlugin>) : PlayerStoreWrapper, Listener, PlayerStore.PlayerStoreDelegate {
    override val store: PlayerStore = PlayerStore()

    init {
        val plugin = plugin.get()
        if (plugin != null) {
            plugin.server?.pluginManager?.registerEvents(this, plugin)
            rebuildStore()
        }
        store.delegate = this
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

    override fun onStoreUpdate(player: Player) {
        serializeToFile(player.uuid, player)
    }

    private fun rebuildStore() {
        store.clear()

        plugin.get()?.server?.onlinePlayers?.forEach { onlinePlayer ->
            val player = deserializeFromFile(uuid = onlinePlayer.uniqueId)
            store.put(onlinePlayer.uniqueId, player)
        }
    }

    private fun deserializeFromFile(uuid: UUID) : Player {
        val folder = plugin.get()?.dataFolder?.absolutePath + File.separator + "players"
        val file = File(folder, "$uuid.yml")

        val reader = YamlConfiguration.loadConfiguration(file)

        return Player(uuid = uuid,
                      isMuted = reader.getBoolean("chat.isMuted"))
    }

    private fun serializeToFile(uuid: UUID, player: Player? = null) {
        val folder = plugin.get()?.dataFolder?.absolutePath + File.separator + "players"
        val file = File(folder, "$uuid.yml")

        val reader = YamlConfiguration.loadConfiguration(file)
        reader.set("uuid", uuid)
        reader.set("chat.isMuted", player?.isMuted ?: false)

        reader.save(file)
    }
}