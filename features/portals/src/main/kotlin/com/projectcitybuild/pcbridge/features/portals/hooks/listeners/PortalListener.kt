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
                    if (settings.sendNoDestinationMessage) {
                        event.player.sendMessage(l10n.portals.noDestination(event.from.world.name))
                    }
                    return
                }
        if (target.world == null) {
            handleUnavailable(event.player, target.target)
            if (settings.bounceBack) event.isCancelled = true
            return
        }
        if (target.target.worldName == event.from.world.name) {
            event.isCancelled = true
            if (settings.sendDisabledPortalMessage) event.player.sendMessage(l10n.portals.disabled)
            return
        }

        val destination = destinationLocation(event.player, event.from, event.to, target.world, type)
        if (type == PortalLinkType.END && event.from.world.environment == World.Environment.THE_END) {
            event.canCreatePortal = false
        }
        if (type == PortalLinkType.END && target.world.environment == World.Environment.THE_END) {
            prepareEndPlatform(target.world)
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
        val target = resolve(event.from.world, type) ?: return
        if (target.world == null) {
            if (settings.bounceBack) event.isCancelled = true
            return
        }
        if (target.target.worldName == event.from.world.name) {
            event.isCancelled = true
            return
        }

        val destination = destinationLocation(event.entity, event.from, event.to, target.world, type)
        if (type == PortalLinkType.END && target.world.environment == World.Environment.THE_END) {
            prepareEndPlatform(target.world)
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
            }
        return DestinationResolution(target, world)
    }

    private fun destinationLocation(
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

        val scale = from.world.coordinateScale / destinationWorld.coordinateScale
        val current = currentTo ?: from
        val y = current.y.coerceIn(
            (destinationWorld.minHeight + 1).toDouble(),
            (destinationWorld.maxHeight - 1).toDouble(),
        )
        return Location(
            destinationWorld,
            floor(from.x * scale),
            y,
            floor(from.z * scale),
            current.yaw,
            current.pitch,
        )
    }

    private fun handleUnavailable(
        player: Player,
        target: PortalTarget,
    ) {
        if (target.explicit && settings.sendNoDestinationMessage) {
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

    private fun prepareEndPlatform(world: World) {
        val spawn = world.spawnLocation
        val x = spawn.blockX
        val y = spawn.blockY - 1
        val z = spawn.blockZ
        if (y < world.minHeight) return

        for (offsetX in -2..2) {
            for (offsetZ in -2..2) {
                val platformBlock = world.getBlockAt(x + offsetX, y, z + offsetZ)
                if (platformBlock.type != Material.OBSIDIAN) {
                    if (settings.endPlatformDropBlocks && platformBlock.type != Material.AIR) {
                        platformBlock.breakNaturally()
                    }
                    platformBlock.type = Material.OBSIDIAN
                }
                for (offsetY in 1..3) {
                    val block = world.getBlockAt(x + offsetX, y + offsetY, z + offsetZ)
                    if (block.type != Material.AIR) {
                        if (settings.endPlatformDropBlocks) block.breakNaturally()
                        block.type = Material.AIR
                    }
                }
            }
        }
    }

    private fun unavailableMessage(world: String) = l10n.portals.destinationUnavailable(world)

    private data class DestinationResolution(
        val target: PortalTarget,
        val world: World?,
    )
}
