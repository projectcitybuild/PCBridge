// package com.projectcitybuild.modules.joinmessages.listeners
//
// import com.projectcitybuild.core.config.PluginConfig
// import com.projectcitybuild.modules.joinmessages.PlayerJoinTimeCache
// import com.projectcitybuild.pcbridge.core.modules.config.Config
// import com.projectcitybuild.support.spigot.SpigotServer
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import net.md_5.bungee.api.chat.TextComponent
// import org.bukkit.event.EventHandler
// import org.bukkit.event.player.PlayerJoinEvent
//
// class AnnounceJoinListener(
//     private val server: SpigotServer,
//     private val config: Config<PluginConfig>,
//     private val playerJoinTimeCache: PlayerJoinTimeCache,
// ) : SpigotListener<PlayerJoinEvent> {
//
//     @EventHandler
//     override suspend fun handle(event: PlayerJoinEvent) {
//         playerJoinTimeCache.put(event.player.uniqueId)
//
//         val message = config.get().messages.join
//             .replace("%name%", event.player.name)
//
//         server.broadcastMessage(
//             TextComponent(message)
//         )
//     }
// }
