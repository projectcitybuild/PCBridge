package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockMultiPlaceEvent
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
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
    ) {
        val position get() = SafeYLookupPosition(worldId, x, z)
    }

    private data class SafeYLookupPosition(
        val worldId: UUID,
        val x: Int,
        val z: Int,
    )

    private companion object {
        val BORDER_FEEDBACK_COOLDOWN = 1.seconds
        val SAFE_Y_LOOKUP_COOLDOWN = 1.seconds
    }

    private val lastBorderFeedback = mutableMapOf<UUID, BorderFeedback>()
    private val safeYLookups = mutableMapOf<UUID, SafeYLookup>()
    private val safeYLookupPlayersByPosition = mutableMapOf<SafeYLookupPosition, MutableSet<UUID>>()

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
        invalidateSafeYLookups(event.world.uid)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) = worldBorderService.relocateIfOutside(event.player)

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        lastBorderFeedback.remove(event.player.uniqueId)
        removeSafeYLookup(event.player.uniqueId)
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
    fun onLivingEntityMove(event: EntityMoveEvent) {
        val from = event.from
        val destination = event.to
        if (from.x == destination.x && from.z == destination.z) return

        val border = worldBorderRepository.get(destination.world) ?: return
        val geometry = border.geometry
        if (geometry.contains(destination.x, destination.z)) return

        val (x, z) = geometry.clamp(destination.x, destination.z)
        event.to = destination.clone().apply {
            this.x = x
            this.z = z
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
        val placedBlocks = if (event is BlockMultiPlaceEvent) {
            event.replacedBlockStates.map { it.block }
        } else {
            listOf(event.blockPlaced)
        }
        invalidateSafeYLookup(event.blockPlaced)
        invalidateSafeYLookups(placedBlocks)
        val border = worldBorderRepository.get(event.blockPlaced.world) ?: return
        if (placedBlocks.any { !border.geometry.containsBlock(it) }) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        invalidateSafeYLookup(event.block)
        val border = worldBorderRepository.get(event.block.world) ?: return
        if (!border.geometry.containsBlock(event.block)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        cancelBucketActionOutside(event, event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBucketFill(event: PlayerBucketFillEvent) {
        cancelBucketActionOutside(event, event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onFluidFlow(event: BlockFromToEvent) {
        invalidateSafeYLookup(event.block)
        invalidateSafeYLookup(event.toBlock)
        val geometry = worldBorderRepository.get(event.block.world)?.geometry ?: return
        val sourceInside = geometry.containsBlock(event.block)
        val destinationInside = geometry.containsBlock(event.toBlock)
        if (sourceInside != destinationInside) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        enforcePistonBorder(event, extending = true)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        enforcePistonBorder(event, extending = false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val location = event.entity.location
        val border = worldBorderRepository.get(location.world) ?: return
        if (!border.geometry.contains(location.x, location.z)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val location = event.entity.location
        val border = worldBorderRepository.get(location.world) ?: return
        if (!border.geometry.contains(location.x, location.z)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        invalidateSafeYLookups(event.blockList())
        val border = worldBorderRepository.get(event.location.world) ?: return
        val geometry = border.geometry
        if (!geometry.contains(event.location.x, event.location.z)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { !geometry.containsBlock(it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        invalidateSafeYLookup(event.block)
        invalidateSafeYLookups(event.blockList())
        val border = worldBorderRepository.get(event.block.world) ?: return
        val geometry = border.geometry
        if (!geometry.containsBlock(event.block)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { !geometry.containsBlock(it) }
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

    private fun cancelBucketActionOutside(
        event: Cancellable,
        block: Block,
    ) {
        invalidateSafeYLookup(block)
        val border = worldBorderRepository.get(block.world) ?: return
        if (!border.geometry.containsBlock(block)) event.isCancelled = true
    }

    private fun enforcePistonBorder(
        event: BlockPistonEvent,
        extending: Boolean,
    ) {
        val movement = if (extending) event.direction else event.direction.oppositeFace
        val movedBlocks = when (event) {
            is BlockPistonExtendEvent -> event.blocks
            is BlockPistonRetractEvent -> event.blocks
            else -> emptyList()
        }
        invalidateSafeYLookup(event.block)
        invalidateSafeYLookup(event.block.getRelative(movement))
        invalidateSafeYLookups(movedBlocks)
        invalidateSafeYLookups(movedBlocks.map { it.getRelative(movement) })
        val geometry = worldBorderRepository.get(event.block.world)?.geometry ?: return
        val headOutside = extending && !geometry.containsBlock(event.block.getRelative(event.direction))
        val movedBlockOutside = movedBlocks.any { block ->
            !geometry.containsBlock(block) || !geometry.containsBlock(block.getRelative(movement))
        }
        if (!geometry.containsBlock(event.block) || headOutside || movedBlockOutside) {
            event.isCancelled = true
        }
    }

    private fun safeBorderLocation(
        geometry: WorldBorderGeometry,
        playerId: UUID,
        location: Location,
    ): Location? = teleportationTracer.traceSync("WorldBorderListener.safeYCache") { span ->
        val clamped = clampedLocation(geometry, location)
        val now = System.nanoTime()
        val lookup = safeYLookups[playerId]?.takeIf {
            it.worldId == clamped.world.uid &&
                it.x == clamped.blockX &&
                it.z == clamped.blockZ &&
                (now - it.lookedUpAtNanos).nanoseconds < SAFE_Y_LOOKUP_COOLDOWN
        }
        span.setAttribute("safe_y.cache_hit", lookup != null)
        span.setAttribute("safe_y.cached_result_available", lookup?.y != null)
        val safeY = if (lookup != null) {
            lookup.y
        } else {
            safeYLocationFinder.findY(clamped.world, clamped.blockX, clamped.blockZ)
        }
        storeSafeYLookup(
            playerId,
            SafeYLookup(
                worldId = clamped.world.uid,
                x = clamped.blockX,
                z = clamped.blockZ,
                y = safeY,
                lookedUpAtNanos = now,
            ),
        )
        safeY ?: return@traceSync null
        clamped.apply { y = safeY.toDouble() }
    }

    private fun storeSafeYLookup(
        playerId: UUID,
        lookup: SafeYLookup,
    ) {
        removeSafeYLookup(playerId)
        safeYLookups[playerId] = lookup
        safeYLookupPlayersByPosition.getOrPut(lookup.position) { mutableSetOf() }.add(playerId)
    }

    private fun removeSafeYLookup(playerId: UUID) {
        val lookup = safeYLookups.remove(playerId) ?: return
        val players = safeYLookupPlayersByPosition[lookup.position] ?: return
        players.remove(playerId)
        if (players.isEmpty()) safeYLookupPlayersByPosition.remove(lookup.position)
    }

    private fun invalidateSafeYLookup(block: Block) {
        invalidateSafeYLookup(SafeYLookupPosition(block.world.uid, block.x, block.z))
    }

    private fun invalidateSafeYLookups(blocks: Iterable<Block>) {
        blocks.forEach(::invalidateSafeYLookup)
    }

    private fun invalidateSafeYLookups(worldId: UUID) {
        safeYLookupPlayersByPosition.keys
            .filter { it.worldId == worldId }
            .forEach(::invalidateSafeYLookup)
    }

    private fun invalidateSafeYLookup(position: SafeYLookupPosition) {
        safeYLookupPlayersByPosition.remove(position)?.forEach { safeYLookups.remove(it) }
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

private fun WorldBorderGeometry.containsBlock(block: Block): Boolean = contains(
    x = block.x + 0.5,
    z = block.z + 0.5,
)
