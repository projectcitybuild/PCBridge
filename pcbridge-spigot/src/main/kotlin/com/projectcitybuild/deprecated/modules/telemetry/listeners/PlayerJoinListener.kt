// package com.projectcitybuild.modules.telemetry.listeners
//
// import com.projectcitybuild.repositories.TelemetryRepository
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import org.bukkit.event.EventHandler
// import org.bukkit.event.player.PlayerJoinEvent
//
// class PlayerJoinListener(
//     private val telemetryRepository: TelemetryRepository,
// ) : SpigotListener<PlayerJoinEvent> {
//
//     @EventHandler
//     override suspend fun handle(event: PlayerJoinEvent) {
//         telemetryRepository.playerSeen(
//             playerUUID = event.player.uniqueId,
//             playerName = event.player.name,
//         )
//     }
// }
