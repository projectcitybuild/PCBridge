package com.projectcitybuild.pcbridge.features.spawns.hooks.listeners

import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.runtime.listeners.traced
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerRespawnListener(
    private val spawnRepository: SpawnRepository,
) : Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerRespawn(event: PlayerRespawnEvent) =
        event.traced(spawnsTracer, this::class.java) {
            if (event.isBedSpawn || event.isAnchorSpawn) return@traced

            val world = event.respawnLocation.world
            val spawn = spawnRepository.get(world)
            event.respawnLocation = spawn
        }
}
