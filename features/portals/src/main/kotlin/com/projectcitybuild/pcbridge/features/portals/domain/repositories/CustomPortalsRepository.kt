package com.projectcitybuild.pcbridge.features.portals.domain.repositories

import com.projectcitybuild.pcbridge.features.portals.domain.data.CustomPortal
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalRegion
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CustomPortalsRepository(
    private val portalsFile: Path,
) {
    private val lock = ReentrantLock()

    fun all(): List<CustomPortal> = lock.withLock {
        val section = load().getConfigurationSection("portals") ?: return emptyList()
        section.getKeys(false).mapNotNull { name ->
            val portal = section.getConfigurationSection(name) ?: return@mapNotNull null
            val rawLocation = portal.getString("location").orEmpty()
            val legacyWorld = portal.getString("world")
            val location =
                if (rawLocation.count { it == ':' } == 1 && legacyWorld != null) {
                    "$legacyWorld:$rawLocation"
                } else {
                    rawLocation
                }
            val region = PortalRegion.parse(location) ?: return@mapNotNull null
            CustomPortal(
                name = name,
                region = region,
                destination =
                    portal.getString("action.value")
                        ?: portal.getString("destination")
                        ?: portal.getString("dest")
                        ?: "",
                actionType =
                    portal.getString("action.type")
                        ?: portal.getString("action-type")
                        ?: "multiverse-destination",
                owner = portal.getString("owner").orEmpty(),
                safeTeleport =
                    portal.getBoolean(
                        "safe-teleport",
                        portal.getBoolean("safeteleport", true),
                    ),
                teleportNonPlayers =
                    portal.getBoolean(
                        "teleport-non-players",
                        portal.getBoolean("teleportnonplayers", false),
                    ),
                entryFee = portal.getDouble("entry-fee.price", portal.getDouble("price", 0.0)),
                noPermissionMessage =
                    portal.getString("message.no-permission")
                        ?: portal.getString("no-permission-message")
                        ?: "@default",
                actionSuccessMessage =
                    portal.getString("message.action-success")
                        ?: portal.getString("action-success-message")
                        ?: "@disabled",
            )
        }
    }

    fun get(name: String): CustomPortal? = all().firstOrNull { it.name == name }

    fun create(
        name: String,
        region: PortalRegion,
        destination: String = "",
        owner: String = "",
    ) = lock.withLock {
        requireSafeName(name)
        val configuration = load()
        val portals = configuration.getConfigurationSection("portals") ?: configuration.createSection("portals")
        require(!portals.contains(name)) { "Portal $name already exists" }
        val portal = portals.createSection(name)
        portal.set("owner", owner)
        portal.set("location", region.serialize())
        portal.set("action.type", "multiverse-destination")
        portal.set("action.value", destination)
        portal.set("version", 1.3)
        save(configuration)
    }

    fun setDestination(
        name: String,
        destination: String,
    ) = lock.withLock {
        requireSafeName(name)
        val configuration = load()
        val portal = configuration.getConfigurationSection("portals.$name")
            ?: throw IllegalArgumentException("Portal $name does not exist")
        portal.set("action.type", "multiverse-destination")
        portal.set("action.value", destination)
        save(configuration)
    }

    fun remove(name: String): Boolean = lock.withLock {
        requireSafeName(name)
        val configuration = load()
        if (!configuration.contains("portals.$name")) return false
        configuration.set("portals.$name", null)
        save(configuration)
        true
    }

    private fun requireSafeName(name: String) {
        require(name.matches(Regex("[a-zA-Z0-9_-]+"))) {
            "Portal names can contain only letters, numbers, underscores, and hyphens"
        }
    }

    private fun load(): YamlConfiguration {
        val configuration = YamlConfiguration()
        if (Files.exists(portalsFile)) configuration.load(portalsFile.toFile())
        return configuration
    }

    private fun save(configuration: YamlConfiguration) {
        val parent = portalsFile.toAbsolutePath().parent
        Files.createDirectories(parent)
        val temporary = Files.createTempFile(parent, "${portalsFile.fileName}.", ".tmp")
        try {
            Files.writeString(temporary, configuration.saveToString(), UTF_8)
            try {
                Files.move(
                    temporary,
                    portalsFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, portalsFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
