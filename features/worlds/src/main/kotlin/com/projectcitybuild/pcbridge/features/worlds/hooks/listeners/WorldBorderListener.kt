package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

class WorldBorderListener(
    private val worldBorderRepository: WorldBorderRepository,
    private val worldBorderService: WorldBorderService,
    private val safeYLocationFinder: SafeYLocationFinder,
) : Listener {
    private data class BorderFeedback(
        val worldId: UUID,
        val sentAtNanos: Long,
    )

    private data class SafeYLookup(
        val worldId: UUID,
        val x: Int,
        val z: Int,
        val y: Int?,
        val lookedUpAtNanos: Long,
    )

    private companion object {
        val BORDER_FEEDBACK_COOLDOWN = 1.seconds
        val SAFE_Y_LOOKUP_COOLDOWN = 1.seconds
    }

    private val lastBorderFeedback = mutableMapOf<UUID, BorderFeedback>()
    private val safeYLookups = mutableMapOf<UUID, SafeYLookup>()

    init {
        worldBorderService.loadAllLoaded()
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        worldBorderService.load(event.world)
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        worldBorderRepository.unload(event.world)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val border = worldBorderRepository.get(player.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(player.location.x, player.location.z)) {
            val destination = safeBorderLocation(geometry, player.uniqueId, player.location)
                ?: clampedLocation(geometry, player.location)
            player.teleport(destination)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        lastBorderFeedback.remove(event.player.uniqueId)
        safeYLookups.remove(event.player.uniqueId)
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
        val geometry = border.geometry

        if (!geometry.contains(destination.x, destination.z)) {
            sendBorderFeedback(event.player, from)
            val safeDestination = safeBorderLocation(geometry, event.player.uniqueId, destination)
            if (safeDestination == null) {
                event.to = clampedLocation(geometry, from)
            } else {
                event.to = safeDestination
            }
        }
    }

    private fun sendBorderFeedback(
        player: Player,
        location: Location,
    ) {
        val now = System.nanoTime()
        val lastSent = lastBorderFeedback[player.uniqueId]
        if (lastSent?.worldId == location.world.uid &&
            (now - lastSent.sentAtNanos).nanoseconds < BORDER_FEEDBACK_COOLDOWN
        ) {
            return
        }

        lastBorderFeedback[player.uniqueId] = BorderFeedback(
            worldId = location.world.uid,
            sentAtNanos = now,
        )
        player.sendMessage(l10n.worlds.borderReached)
        player.playSound(location, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val destination = event.respawnLocation
        val border = worldBorderRepository.get(destination.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(destination.x, destination.z)) {
            event.respawnLocation = safeBorderLocation(geometry, event.player.uniqueId, destination)
                ?: clampedLocation(geometry, destination)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val border = worldBorderRepository.get(event.location.world) ?: return
        if (!border.geometry.contains(event.location.x, event.location.z)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityTeleport(event: EntityTeleportEvent) {
        if (event.entity is Player) return
        val destination = event.to ?: return
        val border = worldBorderRepository.get(destination.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(destination.x, destination.z)) {
            val (x, z) = geometry.clamp(destination.x, destination.z)
            event.to = destination.clone().apply {
                this.x = x
                this.z = z
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onVehicleMove(event: VehicleMoveEvent) {
        val destination = event.to
        val border = worldBorderRepository.get(destination.world) ?: return
        val geometry = border.geometry
        if (geometry.contains(destination.x, destination.z)) return

        val safeLocation = if (geometry.contains(event.from.x, event.from.z)) {
            event.from
        } else {
            val (x, z) = geometry.clamp(destination.x, destination.z)
            destination.clone().apply {
                this.x = x
                this.z = z
            }
        }
        event.vehicle.teleport(safeLocation)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val location = event.blockPlaced.location
        val border = worldBorderRepository.get(location.world) ?: return
        if (!border.geometry.contains(location.x + 0.5, location.z + 0.5)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val location = event.entity.location
        val border = worldBorderRepository.get(location.world) ?: return
        if (!border.geometry.contains(location.x, location.z)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val border = worldBorderRepository.get(event.location.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(event.location.x, event.location.z)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { !geometry.contains(it.x + 0.5, it.z + 0.5) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        val border = worldBorderRepository.get(event.block.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(event.block.x + 0.5, event.block.z + 0.5)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { !geometry.contains(it.x + 0.5, it.z + 0.5) }
    }

    private fun safeBorderLocation(
        geometry: WorldBorderGeometry,
        playerId: UUID,
        location: Location,
    ): Location? {
        val clamped = clampedLocation(geometry, location)
        val now = System.nanoTime()
        val lookup = safeYLookups[playerId]?.takeIf {
            it.worldId == clamped.world.uid &&
                it.x == clamped.blockX &&
                it.z == clamped.blockZ &&
                (now - it.lookedUpAtNanos).nanoseconds < SAFE_Y_LOOKUP_COOLDOWN
        }
        val safeY = if (lookup != null) {
            lookup.y
        } else {
            safeYLocationFinder.findY(clamped.world, clamped.blockX, clamped.blockZ)
        }
        safeYLookups[playerId] = SafeYLookup(
            worldId = clamped.world.uid,
            x = clamped.blockX,
            z = clamped.blockZ,
            y = safeY,
            lookedUpAtNanos = now,
        )
        safeY ?: return null
        return clamped.apply { y = safeY.toDouble() }
    }

    private fun clampedLocation(
        geometry: WorldBorderGeometry,
        location: Location,
    ): Location {
        val (x, z) = geometry.clamp(location.x, location.z)
        return location.clone().apply {
            this.x = x
            this.z = z
        }
    }
}
