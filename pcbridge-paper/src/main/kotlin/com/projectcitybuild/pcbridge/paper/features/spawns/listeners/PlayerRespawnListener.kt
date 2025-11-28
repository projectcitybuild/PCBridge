package com.projectcitybuild.pcbridge.paper.features.spawns.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerRespawnListener(
    private val spawnRepository: SpawnRepository,
): Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerRespawn(
        event: PlayerRespawnEvent,
    ) = event.scoped(spawnsTracer, this::class.java) {
        if (event.isBedSpawn || event.isAnchorSpawn) return@scoped

        val world = event.respawnLocation.world
        val spawn = spawnRepository.get(world)
        event.respawnLocation = spawn
    }
}