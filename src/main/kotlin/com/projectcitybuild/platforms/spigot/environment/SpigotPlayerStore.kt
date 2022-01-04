//package com.projectcitybuild.platforms.spigot.environment
//
//import com.projectcitybuild.modules.playercache.PlayerStore
//import com.projectcitybuild.entities.CachedPlayer
//import org.bukkit.configuration.file.YamlConfiguration
//import org.bukkit.event.EventHandler
//import org.bukkit.event.Listener
//import org.bukkit.event.player.PlayerJoinEvent
//import org.bukkit.event.player.PlayerQuitEvent
//import org.bukkit.plugin.java.JavaPlugin
//import java.io.File
//import java.util.*
//
//class SpigotPlayerStore(val plugin: JavaPlugin, val store: PlayerStore): Listener, PlayerStore.PlayerStoreDelegate {
//    init {
//        plugin.server?.pluginManager?.registerEvents(this, plugin)
//        rebuildStore()
//        store.delegate = this
//    }
//
//    @EventHandler
//    fun playerJoined(event: PlayerJoinEvent) {
////        val player = event.player.makeModel()
////        store.put(event.player.uniqueId, player)
//    }
//
//    @EventHandler
//    fun playerLeft(event: PlayerQuitEvent) {
//        store.remove(event.player.uniqueId)
//    }
//
//    override fun onStoreUpdate(player: CachedPlayer) {
//        serializeToFile(player.uuid, player)
//    }
//
//    private fun rebuildStore() {
//        store.clear()
//
//        plugin.server?.onlinePlayers?.forEach { onlinePlayer ->
//            val player = deserializeFromFile(uuid = onlinePlayer.uniqueId)
//            store.put(onlinePlayer.uniqueId, player)
//        }
//    }
//
//    private fun deserializeFromFile(uuid: UUID) : CachedPlayer {
//        val folder = plugin.dataFolder?.absolutePath + File.separator + "players"
//        val file = File(folder, "$uuid.yml")
//
//        val reader = YamlConfiguration.loadConfiguration(file)
//
//        return CachedPlayer(
//                uuid = uuid,
//                isMuted = reader.getBoolean("chat.isMuted")
//        )
//    }
//
//    private fun serializeToFile(uuid: UUID, player: CachedPlayer? = null) {
//        val folder = plugin.dataFolder?.absolutePath + File.separator + "players"
//        val file = File(folder, "$uuid.yml")
//
//        val reader = YamlConfiguration.loadConfiguration(file)
//        reader.set("uuid", uuid.toString())
//        reader.set("chat.isMuted", player?.isMuted ?: false)
//
//        reader.save(file)
//    }
//}