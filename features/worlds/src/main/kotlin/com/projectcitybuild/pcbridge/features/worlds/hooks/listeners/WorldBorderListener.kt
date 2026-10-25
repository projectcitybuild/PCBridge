package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.WorldLoadEvent

class WorldBorderListener(
    private val worldBorderRepository: WorldBorderRepository,
    private val safeYLocationFinder: SafeYLocationFinder,
) : Listener {
    init {
        worldBorderRepository.loadAllLoaded()
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        worldBorderRepository.load(event.world)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val border = worldBorderRepository.get(player.world) ?: return
        if (!border.contains(player.location.x, player.location.z)) {
            safeBorderLocation(border, player.location)?.let(player::teleport)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event is PlayerTeleportEvent) return

        enforceBorder(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        enforceBorder(event)
    }

    private fun enforceBorder(event: PlayerMoveEvent) {
        val from = event.from
        val destination = event.to ?: return

        if (event !is PlayerTeleportEvent &&
            from.world === destination.world &&
            from.x == destination.x &&
            from.z == destination.z
        ) {
            return
        }

        val border = worldBorderRepository.get(destination.world) ?: return
        if (event !is PlayerTeleportEvent && border.effectiveShape == WorldBorderShape.SQUARE) return

        if (!border.contains(destination.x, destination.z)) {
            val safeDestination = safeBorderLocation(border, destination)
            if (safeDestination == null) {
                event.isCancelled = true
            } else {
                event.to = safeDestination
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val destination = event.respawnLocation
        val border = worldBorderRepository.get(destination.world) ?: return
        if (!border.contains(destination.x, destination.z)) {
            safeBorderLocation(border, destination)?.let { event.respawnLocation = it }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val border = worldBorderRepository.get(event.location.world) ?: return
        if (!border.contains(event.location.x, event.location.z)) {
            event.isCancelled = true
        }
    }

    private fun safeBorderLocation(
        border: SerializableWorldBorder,
        location: Location,
    ): Location? {
        val clamped = border.clamp(location)
        val safeY = safeYLocationFinder.findY(clamped.world, clamped.blockX, clamped.blockZ) ?: return null
        return clamped.apply { y = safeY.toDouble() }
    }
}
