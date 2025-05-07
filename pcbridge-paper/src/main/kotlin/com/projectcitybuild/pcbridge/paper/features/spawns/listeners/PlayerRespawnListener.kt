package com.projectcitybuild.pcbridge.paper.features.spawns.listeners

import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerRespawnListener(
    private val spawnRepository: SpawnRepository,
): Listener {
    @EventHandler
    suspend fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (event.isBedSpawn || event.isAnchorSpawn) return

        val world = event.respawnLocation.world
        val spawn = spawnRepository.get(world)
        event.respawnLocation = spawn
    }
}