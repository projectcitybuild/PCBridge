package com.projectcitybuild.pcbridge.features.portals.domain.repositories

import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLink
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLinkType
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalTarget
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldDefinitionRepository
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PortalLinksRepository(
    private val linksFile: Path,
    private val legacyConfigFile: Path = linksFile.resolveSibling("config.yml"),
    private val settings: PortalSettings,
    private val worldDefinitionRepository: WorldDefinitionRepository,
    private val multiverseNetherPortalsEnabled: () -> Boolean = { false },
) {
    private val lock = ReentrantLock()

    fun all(): List<PortalLink> = lock.withLock {
        val links =
            readLegacyLinks().mapValues { (_, values) -> values.toMutableMap() }.toMutableMap()
        val configuration = load()
        configuration.getKeys(false).forEach { encodedWorld ->
            val section = configuration.getConfigurationSection(encodedWorld) ?: return@forEach
            val source = decodeKey(encodedWorld)
            PortalLinkType.entries.forEach { type ->
                section.getString(type.configKey)?.let { destination ->
                    links.getOrPut(source, ::mutableMapOf)[type] = destination
                }
            }
        }
        links.flatMap { (source, values) ->
            values.map { (type, destination) -> PortalLink(source, type, destination) }
        }
    }

    fun get(
        sourceWorld: String,
        type: PortalLinkType,
    ): String? = lock.withLock {
        load()
            .getConfigurationSection(encodeKey(sourceWorld))
            ?.getString(type.configKey)
            ?: readLegacyLinks()[sourceWorld]?.get(type)
    }

    fun link(
        sourceWorld: String,
        destinationWorld: String,
        type: PortalLinkType,
        bidirectional: Boolean,
    ) {
        requireRegistered(sourceWorld)
        requireRegistered(destinationWorld)
        require(!multiverseNetherPortalsEnabled()) {
            "Disable Multiverse-NetherPortals before changing its portal links"
        }
        lock.withLock {
            val configuration = load()
            setLink(configuration, sourceWorld, destinationWorld, type)
            if (bidirectional) setLink(configuration, destinationWorld, sourceWorld, type)
            save(configuration)
        }
    }

    fun unlink(
        sourceWorld: String,
        type: PortalLinkType,
        bidirectional: Boolean,
    ): Boolean = lock.withLock {
        require(!multiverseNetherPortalsEnabled()) {
            "Disable Multiverse-NetherPortals before changing its portal links"
        }
        val configuration = load()
        val explicitDestination = configuration
            .getConfigurationSection(encodeKey(sourceWorld))
            ?.getString(type.configKey)
        val legacyDestination = readLegacyLinks()[sourceWorld]?.get(type)
        val destination = explicitDestination ?: legacyDestination
        if (destination == null) return false

        var changed = false
        if (legacyDestination != null) {
            setLink(configuration, sourceWorld, sourceWorld, type)
            changed = true
        } else if (removeLink(configuration, sourceWorld, type)) {
            changed = true
        }

        if (bidirectional && destination != sourceWorld) {
            val reverseExplicit =
                configuration
                    .getConfigurationSection(encodeKey(destination))
                    ?.getString(type.configKey)
            val reverseLegacy = readLegacyLinks()[destination]?.get(type)
            if ((reverseExplicit ?: reverseLegacy) == sourceWorld) {
                if (reverseLegacy != null) {
                    setLink(configuration, destination, destination, type)
                    changed = true
                } else if (removeLink(configuration, destination, type)) {
                    changed = true
                }
            }
        }

        if (changed) save(configuration)
        true
    }

    fun resolve(
        sourceWorld: String,
        sourceEnvironment: String,
        type: PortalLinkType,
    ): PortalTarget? {
        val explicit = get(sourceWorld, type)
        if (explicit != null) return PortalTarget(explicit, explicit = true)

        val candidate = when {
            isLinkedDimension(sourceWorld, sourceEnvironment, type) -> {
                removeDimensionName(sourceWorld, type)
            }
            else -> {
                addDimensionName(sourceWorld, type)
            }
        }
        if (candidate == sourceWorld || worldDefinitionRepository.get(candidate) == null) return null
        return PortalTarget(candidate, explicit = false)
    }

    private fun requireRegistered(worldName: String) {
        require(worldDefinitionRepository.get(worldName) != null) {
            "World $worldName is not registered"
        }
    }

    private fun setLink(
        configuration: YamlConfiguration,
        source: String,
        destination: String,
        type: PortalLinkType,
    ) {
        val key = encodeKey(source)
        val section = configuration.getConfigurationSection(key) ?: configuration.createSection(key)
        section.set(type.configKey, destination)
    }

    private fun removeLink(
        configuration: YamlConfiguration,
        source: String,
        type: PortalLinkType,
    ): Boolean {
        if (source.isBlank()) return false
        val key = encodeKey(source)
        val section = configuration.getConfigurationSection(key) ?: return false
        if (!section.contains(type.configKey)) return false
        section.set(type.configKey, null)
        if (section.getKeys(false).isEmpty()) configuration.set(key, null)
        return true
    }

    private fun load(): YamlConfiguration {
        val configuration = YamlConfiguration()
        if (Files.exists(linksFile)) configuration.load(linksFile.toFile())
        return configuration
    }

    private fun readLegacyLinks(): Map<String, Map<PortalLinkType, String>> {
        if (!Files.isRegularFile(legacyConfigFile)) return emptyMap()
        val configuration = YamlConfiguration()
        configuration.load(legacyConfigFile.toFile())
        val worlds = configuration.getConfigurationSection("worlds") ?: return emptyMap()
        val result = mutableMapOf<String, MutableMap<PortalLinkType, String>>()

        fun collect(
            values: Map<String, Any?>,
            parentPath: String,
        ) {
            values.forEach { (worldPart, value) ->
                val worldName = parentPath + worldPart
                val world = value.asStringMap() ?: return@forEach
                val portalLinks = world["portalgoesto"].asStringMap()
                if (portalLinks.isEmpty()) {
                    collect(world, "$worldName.")
                    return@forEach
                }
                val nether = portalLinks["NETHER"]?.toString()
                val end = (portalLinks["ENDER"] ?: portalLinks["END"])?.toString()
                if (nether != null) result.getOrPut(worldName, ::mutableMapOf)[PortalLinkType.NETHER] = nether
                if (end != null) result.getOrPut(worldName, ::mutableMapOf)[PortalLinkType.END] = end
            }
        }

        collect(worlds.getValues(false), "")
        return result
    }

    private fun Any?.asStringMap(): Map<String, Any?> = when (this) {
        is org.bukkit.configuration.ConfigurationSection ->
            getValues(false).mapValues { (_, value) -> value }
        is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value }
        else -> emptyMap()
    }

    private fun save(configuration: YamlConfiguration) {
        val parent = linksFile.toAbsolutePath().parent
        Files.createDirectories(parent)
        val temporary = Files.createTempFile(parent, "${linksFile.fileName}.", ".tmp")
        try {
            Files.writeString(temporary, configuration.saveToString(), UTF_8)
            try {
                Files.move(
                    temporary,
                    linksFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, linksFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun isLinkedDimension(
        name: String,
        environment: String,
        type: PortalLinkType,
    ): Boolean {
        val expectedEnvironment =
            when (type) {
                PortalLinkType.NETHER -> "NETHER"
                PortalLinkType.END -> "THE_END"
            }
        return environment.equals(expectedEnvironment, ignoreCase = true) &&
            removeDimensionName(name, type) != name
    }

    private fun addDimensionName(
        baseName: String,
        type: PortalLinkType,
    ): String {
        val prefix = if (type == PortalLinkType.NETHER) settings.netherPrefix else settings.endPrefix
        val suffix = if (type == PortalLinkType.NETHER) settings.netherSuffix else settings.endSuffix
        return "$prefix$baseName$suffix"
    }

    private fun removeDimensionName(
        name: String,
        type: PortalLinkType,
    ): String {
        val prefix = if (type == PortalLinkType.NETHER) settings.netherPrefix else settings.endPrefix
        val suffix = if (type == PortalLinkType.NETHER) settings.netherSuffix else settings.endSuffix
        if (!name.startsWith(prefix) || !name.endsWith(suffix)) return name
        val endIndex = name.length - suffix.length
        if (endIndex <= prefix.length) return name
        return name.substring(prefix.length, endIndex)
    }

    private fun encodeKey(name: String) = name.replace(".", "[dot]")

    private fun decodeKey(name: String) = name.replace("[dot]", ".")
}
