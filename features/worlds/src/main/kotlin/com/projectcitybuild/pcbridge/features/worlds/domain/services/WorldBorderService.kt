package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderLimits
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import kotlinx.coroutines.future.await
import org.bukkit.Server
import org.bukkit.World

class WorldBorderService(
    private val server: Server,
    private val repository: WorldBorderRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val safeYLocationFinder: SafeYLocationFinder,
) {
    fun load(world: World): WorldBorder? = repository.get(world)?.also { world.applyNativeBorder(it) }

    fun loadAllLoaded() {
        server.worlds.forEach(::load)
    }

    suspend fun set(world: World, border: WorldBorder) {
        repository.set(world, border)
        world.applyNativeBorder(border)
        val geometry = border.geometry
        for (player in server.onlinePlayers) {
            if (player.world.uid != world.uid || geometry.contains(player.location.x, player.location.z)) continue

            val (x, z) = geometry.clamp(player.location.x, player.location.z)
            val destination = player.location.clone().apply {
                this.x = x
                this.z = z
                safeYLocationFinder.findY(world, blockX, blockZ)?.let { y = it.toDouble() }
            }
            check(player.teleportAsync(destination).await()) {
                "Failed to relocate ${player.uniqueId} inside world border for ${world.name}"
            }
        }
        eventBroadcaster.broadcast(WorldBorderUpdatedEvent(world, border))
    }

    suspend fun clear(world: World) {
        repository.clear(world)
        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = WorldBorderLimits.MAX_DIMENSION
        eventBroadcaster.broadcast(WorldBorderUpdatedEvent(world, null))
    }

    private fun World.applyNativeBorder(border: WorldBorder) {
        worldBorder.setCenter(border.centerX, border.centerZ)
        worldBorder.size = border.geometry.nativeBorderSize
    }
}
