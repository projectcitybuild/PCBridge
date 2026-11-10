package com.projectcitybuild.pcbridge.features.portals.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLinkType
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalTarget
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.PortalLinksRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.Plugin
import kotlin.math.floor

class PortalListener(
    private val plugin: Plugin,
    private val server: Server,
    private val portalLinksRepository: PortalLinksRepository,
    private val worldLifecycleService: WorldLifecycleService,
    private val settings: PortalSettings,
) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerPortal(event: PlayerPortalEvent) {
        val type = when (event.cause) {
            TeleportCause.NETHER_PORTAL -> PortalLinkType.NETHER
            TeleportCause.END_PORTAL -> PortalLinkType.END
            else -> return
        }
        val target =
            resolve(event.from.world, type)
                ?: run {
                    event.isCancelled = true
                    plugin.logger.warning("No ${type.configKey} portal destination is registered for ${event.from.world.name}")
                    if (settings.sendNoDestinationMessage) {
                        event.player.sendMessage(l10n.portals.noDestination(event.from.world.name))
                    }
                    return
                }
        if (target.world == null) {
            handleUnavailable(event.player, target.target)
            event.isCancelled = true
            return
        }
        if (target.target.worldName == event.from.world.name) {
            event.isCancelled = true
            if (settings.sendDisabledPortalMessage) event.player.sendMessage(l10n.portals.disabled)
            return
        }

        val endDestination = endPlatformDestination(target.world, type)
        if (type == PortalLinkType.END && target.world.environment == World.Environment.THE_END && endDestination == null) {
            event.isCancelled = true
            plugin.logger.warning("Could not find a safe End platform in ${target.world.name}; portal travel was cancelled")
            if (settings.sendNoDestinationMessage) {
                event.player.sendMessage(unavailableMessage(target.world.name))
            }
            return
        }

        val destination = endDestination ?: try {
            destinationLocation(event.player, event.from, event.to, target.world, type)
        } catch (failure: IllegalArgumentException) {
            event.isCancelled = true
            plugin.logger.warning(
                "Invalid ${type.configKey} portal destination from ${event.from.world.name} " +
                    "to ${target.world.name}: ${failure.message}",
            )
            if (settings.sendNoDestinationMessage) {
                event.player.sendMessage(unavailableMessage(target.world.name))
            }
            return
        }
        if (type == PortalLinkType.END && event.from.world.environment == World.Environment.THE_END) {
            event.canCreatePortal = false
        }
        event.to = destination
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (!settings.handleEndExitRespawn ||
            event.respawnReason != PlayerRespawnEvent.RespawnReason.END_PORTAL
        ) {
            return
        }

        val sourceWorld = event.player.world
        val target = resolve(sourceWorld, PortalLinkType.END) ?: return
        val destinationWorld = target.world ?: return
        if (destinationWorld == sourceWorld || target.target.worldName == sourceWorld.name) return

        val currentRespawn = event.respawnLocation
        if (currentRespawn.world == destinationWorld) return

        val netherTarget = portalLinksRepository.resolve(
            destinationWorld.name,
            destinationWorld.environment.name,
            PortalLinkType.NETHER,
        )
        if (currentRespawn.world?.name == netherTarget?.worldName) return

        event.respawnLocation = destinationWorld.spawnLocation
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityPortal(event: EntityPortalEvent) {
        if (!settings.teleportEntities) return
        val type = portalType(event.from, event.to) ?: return
        val target = resolve(event.from.world, type) ?: run {
            event.isCancelled = true
            plugin.logger.warning("No ${type.configKey} portal destination is registered for ${event.from.world.name}")
            return
        }
        if (target.world == null) {
            event.isCancelled = true
            return
        }
        if (target.target.worldName == event.from.world.name) {
            event.isCancelled = true
            return
        }

        val endDestination = endPlatformDestination(target.world, type)
        if (type == PortalLinkType.END && target.world.environment == World.Environment.THE_END && endDestination == null) {
            event.isCancelled = true
            plugin.logger.warning("Could not find a safe End platform in ${target.world.name}; entity portal travel was cancelled")
            return
        }
        val destination = endDestination ?: try {
            destinationLocation(event.entity, event.from, event.to, target.world, type)
        } catch (failure: IllegalArgumentException) {
            event.isCancelled = true
            plugin.logger.warning(
                "Invalid ${type.configKey} portal destination from ${event.from.world.name} " +
                    "to ${target.world.name}: ${failure.message}",
            )
            return
        }
        event.to = destination
    }

    private fun resolve(
        sourceWorld: World,
        type: PortalLinkType,
    ): DestinationResolution? {
        val target = portalLinksRepository.resolve(
            sourceWorld.name,
            sourceWorld.environment.name,
            type,
        ) ?: return null
        val world = server.getWorld(target.worldName)
            ?: try {
                worldLifecycleService.load(target.worldName)
            } catch (failure: IllegalArgumentException) {
                plugin.logger.warning("Portal destination ${target.worldName} is not registered: ${failure.message}")
                null
            } catch (failure: IllegalStateException) {
                plugin.logger.severe("Could not load portal destination ${target.worldName}: ${failure.message}")
                null
            } catch (failure: Exception) {
                plugin.logger.log(
                    java.util.logging.Level.SEVERE,
                    "Could not resolve portal destination ${target.worldName}",
                    failure,
                )
                null
            }
        return DestinationResolution(target, world)
    }

    private fun endPlatformDestination(
        destinationWorld: World,
        type: PortalLinkType,
    ): Location? {
        if (type != PortalLinkType.END || destinationWorld.environment != World.Environment.THE_END) return null
        return prepareEndPlatform(destinationWorld)
    }

    internal fun destinationLocation(
        entity: Entity,
        from: Location,
        currentTo: Location?,
        destinationWorld: World,
        type: PortalLinkType,
    ): Location {
        if (type == PortalLinkType.END) {
            if (settings.handleEndExitRespawn &&
                from.world.environment == World.Environment.THE_END &&
                entity is Player
            ) {
                entity.respawnLocation
                    ?.takeIf { it.world == destinationWorld }
                    ?.let { return it.clone() }
            }
            return destinationWorld.spawnLocation
        }

        val sourceScale = from.world.coordinateScale
        val destinationScale = destinationWorld.coordinateScale
        require(sourceScale.isFinite() && sourceScale > 0.0 && destinationScale.isFinite() && destinationScale > 0.0) {
            "Portal worlds must have finite, positive coordinate scales"
        }
        val scale = sourceScale / destinationScale
        require(scale.isFinite() && scale > 0.0) {
            "Portal coordinate scale is outside the supported range"
        }
        val current = currentTo ?: from
        val y = current.y.coerceIn(
            (destinationWorld.minHeight + 1).toDouble(),
            (destinationWorld.maxHeight - 1).toDouble(),
        )
        require(y.isFinite()) { "Portal destination has an invalid Y coordinate" }
        val x = floor(from.x * scale)
        val z = floor(from.z * scale)
        require(
            x.isFinite() &&
                z.isFinite() &&
                x >= Int.MIN_VALUE.toDouble() &&
                x <= Int.MAX_VALUE.toDouble() &&
                z >= Int.MIN_VALUE.toDouble() &&
                z <= Int.MAX_VALUE.toDouble(),
        ) {
            "Portal destination coordinates exceed the supported range"
        }
        val destination = Location(
            destinationWorld,
            x,
            y,
            z,
            current.yaw,
            current.pitch,
        )
        require(destinationWorld.worldBorder.isInside(destination)) {
            "Portal destination is outside the world border"
        }
        return destination
    }

    private fun handleUnavailable(
        player: Player,
        target: PortalTarget,
    ) {
        if (settings.sendNoDestinationMessage) {
            player.sendMessage(unavailableMessage(target.worldName))
        }
    }

    private fun portalType(
        from: Location,
        to: Location?,
    ): PortalLinkType? = when {
        from.block.type == Material.END_PORTAL || from.world.environment == World.Environment.THE_END -> {
            PortalLinkType.END
        }
        from.block.type == Material.NETHER_PORTAL || from.world.environment == World.Environment.NETHER -> {
            PortalLinkType.NETHER
        }
        to?.world?.environment == World.Environment.THE_END -> {
            PortalLinkType.END
        }
        to?.world?.environment == World.Environment.NETHER -> {
            PortalLinkType.NETHER
        }
        else -> {
            null
        }
    }

    internal fun prepareEndPlatform(world: World): Location? {
        val spawn = world.spawnLocation
        val platformY = spawn.blockY - 1
        if (platformY < world.minHeight || platformY + 3 >= world.maxHeight) return null

        for ((offsetX, offsetZ) in platformOffsets()) {
            val x = spawn.blockX + offsetX
            val z = spawn.blockZ + offsetZ
            val baseBlocks = mutableListOf<Block>()
            var safe = true

            for (dx in -2..2) {
                for (dz in -2..2) {
                    val blockX = x + dx
                    val blockZ = z + dz
                    if (!world.isChunkLoaded(blockX shr 4, blockZ shr 4) ||
                        !world.worldBorder.isInside(Location(world, blockX + 0.5, platformY.toDouble(), blockZ + 0.5))
                    ) {
                        safe = false
                        break
                    }

                    val base = world.getBlockAt(blockX, platformY, blockZ)
                    if (!base.type.isAirMaterial() && base.type != Material.OBSIDIAN) {
                        safe = false
                        break
                    }
                    if ((1..3).any { offsetY ->
                            !world.getBlockAt(blockX, platformY + offsetY, blockZ).type.isAirMaterial()
                        }
                    ) {
                        safe = false
                        break
                    }
                    baseBlocks += base
                }
                if (!safe) break
            }

            if (!safe) continue
            baseBlocks.filter { it.type.isAirMaterial() }.forEach { it.setType(Material.OBSIDIAN, false) }
            return Location(
                world,
                x + 0.5,
                platformY + 1.0,
                z + 0.5,
                spawn.yaw,
                spawn.pitch,
            )
        }
        return null
    }

    private fun Material.isAirMaterial(): Boolean = this == Material.AIR || this == Material.CAVE_AIR || this == Material.VOID_AIR

    private fun platformOffsets(): Sequence<Pair<Int, Int>> = sequence {
        yield(0 to 0)
        for (radius in 1..MAX_END_PLATFORM_SEARCH_RADIUS) {
            for (x in -radius..radius) {
                yield(x to -radius)
                yield(x to radius)
            }
            for (z in -radius + 1 until radius) {
                yield(-radius to z)
                yield(radius to z)
            }
        }
    }

    private fun unavailableMessage(world: String) = l10n.portals.destinationUnavailable(world)

    private data class DestinationResolution(
        val target: PortalTarget,
        val world: World?,
    )

    private companion object {
        const val MAX_END_PLATFORM_SEARCH_RADIUS = 16
    }
}
