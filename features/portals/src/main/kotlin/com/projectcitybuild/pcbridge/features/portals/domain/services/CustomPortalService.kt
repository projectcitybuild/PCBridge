package com.projectcitybuild.pcbridge.features.portals.domain.services

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.portals.domain.data.CustomPortal
import com.projectcitybuild.pcbridge.features.portals.domain.data.MultiversePortalsSettings
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalRegion
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.CustomPortalsRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CustomPortalService(
    private val plugin: Plugin,
    private val server: Server,
    private val repository: CustomPortalsRepository,
    private val settings: MultiversePortalsSettings,
    private val worldLifecycleService: WorldLifecycleService,
    private val playerTeleporter: PlayerTeleporter,
) {
    private val selections = ConcurrentHashMap<UUID, PortalSelection>()
    private val insidePortals = ConcurrentHashMap<UUID, String>()
    private val pendingTeleports = ConcurrentHashMap<UUID, String>()
    private val lastTeleports = ConcurrentHashMap<UUID, PortalCooldown>()

    @Volatile
    private var portals = repository.all()

    fun all(): List<CustomPortal> = portals

    fun findAt(location: Location): CustomPortal? = portals.firstOrNull { it.region.contains(location) }

    fun beginPortalEntry(
        player: Player,
        location: Location,
        fallbackLocation: Location? = null,
    ): PortalEntry? {
        val portal = findAt(location) ?: fallbackLocation?.let(::findAt)
        if (portal == null) {
            insidePortals.remove(player.uniqueId)
            return null
        }

        val playerId = player.uniqueId
        val previousPortal = insidePortals.put(playerId, portal.name)
        val pending = pendingTeleports[playerId] == portal.name
        val lastTeleport = lastTeleports[playerId]
        if (previousPortal == portal.name) {
            val recentlyUsed = lastTeleport?.portalName == portal.name
            return PortalEntry(
                portal = portal,
                shouldActivate = false,
                cancelVanilla = !settings.portalsDefaultToNether || pending || recentlyUsed,
            )
        }

        if (settings.enforcePortalAccess && !player.hasPermission(accessPermission(portal))) {
            sendNoPermissionMessage(player, portal)
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        if (portal.destination.isBlank()) {
            player.sendMessage(l10n.portals.customDestinationMissing(portal.name))
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        if (!portal.actionType.equals("multiverse-destination", ignoreCase = true)) {
            plugin.logger.warning("Portal ${portal.name} uses unsupported action type ${portal.actionType}")
            player.sendMessage(l10n.portals.customActionUnsupported(portal.name))
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        if (portal.entryFee > 0.0) {
            plugin.logger.warning("Portal ${portal.name} has an entry fee, but no economy provider is configured")
            player.sendMessage(l10n.portals.customActionUnsupported(portal.name))
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        val cooldown = lastTeleport
        if (cooldown != null && remainingCooldownNanos(cooldown) > 0) {
            val remainingMillis = (remainingCooldownNanos(cooldown) + 999_999) / 1_000_000
            player.sendMessage(l10n.portals.customCooldown(remainingMillis))
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        if (pendingTeleports.putIfAbsent(playerId, portal.name) != null) {
            return PortalEntry(portal, shouldActivate = false, cancelVanilla = true)
        }
        return PortalEntry(portal, shouldActivate = true, cancelVanilla = true)
    }

    suspend fun activate(
        player: Player,
        portal: CustomPortal,
    ) {
        val playerId = player.uniqueId
        try {
            check(Bukkit.isPrimaryThread()) { "Custom portal destinations must be resolved on the server thread" }
            val destination = resolveDestination(portal, mutableSetOf())
            playerTeleporter.move(
                player,
                destination,
                cause = TeleportCause.PLUGIN,
                options =
                    PlayerTeleporter.TeleportOptions(
                        adjustYForSafety = portal.safeTeleport,
                    ),
            )
            lastTeleports[playerId] = PortalCooldown(portal.name, System.nanoTime())
            portal.actionSuccessMessage
                .takeUnless { it.equals("@disabled", ignoreCase = true) }
                ?.takeUnless { it.isBlank() }
                ?.let { message ->
                    if (message.equals("@default", ignoreCase = true)) {
                        player.sendMessage(l10n.portals.customTeleportSucceeded(portal.name))
                    } else {
                        player.sendMessage(message)
                    }
                }
        } finally {
            pendingTeleports.remove(playerId, portal.name)
        }
    }

    fun enableWand(player: Player) {
        selections[player.uniqueId] = PortalSelection(wandEnabled = true)
        player.sendMessage(l10n.portals.customWandEnabled(settings.wandMaterial.name))
    }

    fun selectCorner(
        player: Player,
        location: Location,
        first: Boolean,
    ): Boolean {
        val selection = selections[player.uniqueId] ?: return false
        if (!selection.wandEnabled) return false
        val corner = location.block.location
        if (first) {
            selection.first = corner
        } else {
            selection.second = corner
        }
        player.sendMessage(l10n.portals.customSelectionPoint(first, corner.world.name, corner.blockX, corner.blockY, corner.blockZ))
        return true
    }

    fun selectedRegion(player: Player): PortalRegion? {
        val selection = selections[player.uniqueId] ?: return null
        val first = selection.first ?: return null
        val second = selection.second ?: return null
        val firstWorld = first.world ?: return null
        val secondWorld = second.world ?: return null
        if (firstWorld != secondWorld) return null
        return PortalRegion.fromCorners(firstWorld.name, first, second)
    }

    fun isWandEnabled(player: Player): Boolean = selections[player.uniqueId]?.wandEnabled == true

    fun create(
        player: Player,
        name: String,
        destination: String,
    ) {
        val region = selectedRegion(player)
            ?: throw IllegalStateException("Select both portal corners in the same world first")
        if (destination.isNotBlank()) validateDestination(destination)
        repository.create(name, region, destination, player.name)
        refresh()
    }

    fun setDestination(
        portalName: String,
        destination: String,
    ) {
        validateDestination(destination)
        repository.setDestination(portalName, destination)
        refresh()
    }

    fun remove(portalName: String): Boolean {
        val portal = portals.firstOrNull { it.name == portalName } ?: return false
        if (settings.clearOnRemove) clearPortalBlocks(portal.region)
        val removed = repository.remove(portalName)
        if (removed) refresh()
        return removed
    }

    fun entityDestination(portal: CustomPortal): Location? {
        if (!portal.teleportNonPlayers) return null
        require(portal.actionType.equals("multiverse-destination", ignoreCase = true)) {
            "Portal ${portal.name} uses unsupported action type ${portal.actionType}"
        }
        require(portal.entryFee <= 0.0) {
            "Portal ${portal.name} has an entry fee, but no economy provider is configured"
        }
        return resolveDestination(portal, mutableSetOf())
    }

    fun reload() {
        portals = repository.all()
    }

    fun forgetPlayer(player: Player) {
        selections.remove(player.uniqueId)
        insidePortals.remove(player.uniqueId)
        pendingTeleports.remove(player.uniqueId)
        lastTeleports.remove(player.uniqueId)
    }

    private fun resolveDestination(
        portal: CustomPortal,
        visited: MutableSet<String>,
    ): Location {
        check(visited.add(portal.name)) { "Portal destination loop detected at ${portal.name}" }
        require(portal.actionType.equals("multiverse-destination", ignoreCase = true)) {
            "Portal ${portal.name} uses unsupported action type ${portal.actionType}"
        }
        require(portal.entryFee <= 0.0) {
            "Portal ${portal.name} has an entry fee, but no economy provider is configured"
        }
        val tokens = portal.destination.split(':')
        return when (tokens.firstOrNull()?.lowercase()) {
            "w" -> {
                require(tokens.size in 2..3) { "Expected w:<world>[:<direction>] for portal ${portal.name}" }
                val world = resolveWorld(tokens[1])
                world.spawnLocation.clone().apply {
                    if (tokens.size == 3) yaw = directionYaw(tokens[2])
                }
            }
            "e" -> {
                require(tokens.size == 3) { "Expected e:<world>:<x,y,z> for portal ${portal.name}" }
                val world = resolveWorld(tokens[1])
                val coordinates = tokens[2].split(',').map { coordinate ->
                    coordinate.toDoubleOrNull()?.takeIf(Double::isFinite)
                        ?: throw IllegalArgumentException("Invalid coordinates in portal ${portal.name}")
                }
                Location(
                    world,
                    coordinates[0],
                    coordinates[1],
                    coordinates[2],
                )
            }
            "p" -> {
                require(tokens.size in 2..3) { "Expected p:<portal>[:<direction>] for portal ${portal.name}" }
                val target = portals.firstOrNull { it.name == tokens[1] }
                    ?: throw IllegalArgumentException("Portal ${tokens[1]} does not exist")
                resolveDestination(target, visited).apply {
                    if (tokens.size == 3) yaw = directionYaw(tokens[2])
                }
            }
            else -> throw IllegalArgumentException("Unsupported Multiverse destination: ${portal.destination}")
        }
    }

    private fun resolveWorld(name: String): World = server.getWorld(name) ?: worldLifecycleService.load(name)

    private fun directionYaw(direction: String): Float = when (direction.lowercase()) {
        "s", "south" -> 0f
        "sw", "southwest" -> 45f
        "w", "west" -> 90f
        "nw", "northwest" -> 135f
        "n", "north" -> 180f
        "ne", "northeast" -> 225f
        "e", "east" -> 270f
        "se", "southeast" -> 315f
        else -> throw IllegalArgumentException("Unknown portal direction: $direction")
    }

    private fun sendNoPermissionMessage(
        player: Player,
        portal: CustomPortal,
    ) {
        if (!settings.sendNoPermissionMessages ||
            portal.noPermissionMessage.equals("@disabled", ignoreCase = true)
        ) {
            return
        }
        if (portal.noPermissionMessage.equals("@default", ignoreCase = true)) {
            player.sendMessage(l10n.portals.customNoPermission(portal.name))
        } else {
            player.sendMessage(portal.noPermissionMessage)
        }
    }

    private fun accessPermission(portal: CustomPortal) = "multiverse.portal.access.${portal.name}"

    private fun remainingCooldownNanos(cooldown: PortalCooldown): Long =
        settings.portalCooldownMillis * 1_000_000 - (System.nanoTime() - cooldown.createdNanos)

    private fun validateDestination(destination: String) {
        val tokens = destination.split(':')
        when (tokens.firstOrNull()?.lowercase()) {
            "w" -> require(tokens.size in 2..3 && tokens[1].isNotBlank()) {
                "Expected w:<world>[:<direction>] destination"
            }
            "e" -> {
                require(tokens.size == 3 && tokens[1].isNotBlank()) {
                    "Expected e:<world>:<x,y,z> destination"
                }
                val coordinates = tokens[2].split(',')
                require(coordinates.size == 3 && coordinates.all { it.toDoubleOrNull()?.isFinite() == true }) {
                    "Exact destinations require three finite coordinates"
                }
            }
            "p" -> require(tokens.size in 2..3 && tokens[1].isNotBlank()) {
                "Expected p:<portal>[:<direction>] destination"
            }
            else -> throw IllegalArgumentException("Only w:, e:, and p: destinations are supported")
        }
    }

    private fun clearPortalBlocks(region: PortalRegion) {
        val world = server.getWorld(region.worldName) ?: worldLifecycleService.load(region.worldName)
        val width = region.maxX.toLong() - region.minX + 1
        val height = region.maxY.toLong() - region.minY + 1
        val depth = region.maxZ.toLong() - region.minZ + 1
        require(
            width <= MAX_BLOCKS_TO_CLEAR &&
                height <= MAX_BLOCKS_TO_CLEAR / width &&
                depth <= MAX_BLOCKS_TO_CLEAR / (width * height),
        ) {
            "Portal region is too large to clear safely"
        }
        for (x in region.minX..region.maxX) {
            for (y in region.minY..region.maxY) {
                for (z in region.minZ..region.maxZ) {
                    val block = world.getBlockAt(x, y, z)
                    if (block.type == Material.NETHER_PORTAL) block.type = Material.AIR
                }
            }
        }
    }

    private fun refresh() {
        portals = repository.all()
    }

    data class PortalEntry(
        val portal: CustomPortal,
        val shouldActivate: Boolean,
        val cancelVanilla: Boolean,
    )

    private data class PortalCooldown(
        val portalName: String,
        val createdNanos: Long,
    )

    private data class PortalSelection(
        var first: Location? = null,
        var second: Location? = null,
        var wandEnabled: Boolean = false,
    )

    private companion object {
        const val MAX_BLOCKS_TO_CLEAR = 1_000_000L
    }
}
