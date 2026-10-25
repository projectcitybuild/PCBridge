package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.WorldLoadEvent
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

class WorldBorderListener(
    private val worldBorderRepository: WorldBorderRepository,
    private val safeYLocationFinder: SafeYLocationFinder,
) : Listener {
    private val lastBorderFeedback = mutableMapOf<UUID, Long>()

    private companion object {
        val BORDER_FEEDBACK_COOLDOWN = 1.seconds
    }

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
        val geometry = WorldBorderGeometry(border)
        if (!geometry.contains(player.location.x, player.location.z)) {
            safeBorderLocation(geometry, player.location)?.let(player::teleport)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        lastBorderFeedback.remove(event.player.uniqueId)
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
        val geometry = WorldBorderGeometry(border)

        if (!geometry.contains(destination.x, destination.z)) {
            sendBorderFeedback(event.player, from)
            val safeDestination = safeBorderLocation(geometry, destination)
            if (safeDestination == null) {
                event.isCancelled = true
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
        if (lastSent != null && (now - lastSent).nanoseconds < BORDER_FEEDBACK_COOLDOWN) return

        lastBorderFeedback[player.uniqueId] = now
        player.sendMessage(l10n.worlds.borderReached)
        player.playSound(location, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val destination = event.respawnLocation
        val border = worldBorderRepository.get(destination.world) ?: return
        val geometry = WorldBorderGeometry(border)
        if (!geometry.contains(destination.x, destination.z)) {
            safeBorderLocation(geometry, destination)?.let { event.respawnLocation = it }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val border = worldBorderRepository.get(event.location.world) ?: return
        if (!WorldBorderGeometry(border).contains(event.location.x, event.location.z)) {
            event.isCancelled = true
        }
    }

    private fun safeBorderLocation(
        geometry: WorldBorderGeometry,
        location: Location,
    ): Location? {
        val clamped = geometry.clamp(location)
        val safeY = safeYLocationFinder.findY(clamped.world, clamped.blockX, clamped.blockZ) ?: return null
        return clamped.apply { y = safeY.toDouble() }
    }
}
