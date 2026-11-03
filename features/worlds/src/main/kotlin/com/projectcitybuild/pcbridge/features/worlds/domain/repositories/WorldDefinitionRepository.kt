package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldSpawnLocation
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class WorldDefinitionRepository(
    private val worldsFile: Path,
    private val isWorldLoaded: (String) -> Boolean,
    private val multiverseCoreEnabled: () -> Boolean = { false },
) {
    private val lock = ReentrantLock()

    fun all(): List<WorldRegistration> = lock.withLock {
        val configuration = load()
        configuration.getKeys(false).mapNotNull { encodedKey ->
            val raw = configuration.get(encodedKey) ?: return@mapNotNull null
            val properties = raw.asStringMap() ?: return@mapNotNull null
            val definition = properties.toDefinition(encodedKey)
            WorldRegistration(
                definition = definition,
                isLoaded = isWorldLoaded(definition.name),
            )
        }
    }

    fun get(registryKey: String): WorldRegistration? = all().firstOrNull {
        it.definition.registryKey == registryKey ||
            it.definition.name == registryKey
    }

    fun save(definition: WorldDefinition) {
        require(definition.registryKey.isNotBlank()) { "World registry key must not be blank" }
        require(definition.name.isNotBlank()) { "World name must not be blank" }
        requireWriteAccess()
        lock.withLock {
            val configuration = load()
            val rootValues =
                configuration.getValues(false)
                    .mapValues { (_, value) -> value.toPlainValue() }
                    .toMutableMap()
            val properties = definition.properties.toMutableMap()
            applyKnownProperties(properties, definition)
            rootValues[encodeKey(definition.registryKey)] = properties
            write(rootValues)
        }
    }

    fun unregister(registryKey: String): Boolean {
        requireWriteAccess()
        return lock.withLock {
            val configuration = load()
            val encodedKey = encodeKey(registryKey)
            if (!configuration.contains(encodedKey)) return false

            val rootValues =
                configuration.getValues(false)
                    .mapValues { (_, value) -> value.toPlainValue() }
                    .toMutableMap()
            rootValues.remove(encodedKey)
            write(rootValues)
            true
        }
    }

    fun requireWriteAccess() {
        check(!multiverseCoreEnabled()) {
            "Disable Multiverse-Core before managing worlds with PCBridge"
        }
    }

    private fun load(): YamlConfiguration {
        val configuration = YamlConfiguration()
        if (Files.exists(worldsFile)) {
            configuration.load(worldsFile.toFile())
        }
        return configuration
    }

    private fun write(rootValues: Map<String, Any?>) {
        val configuration = YamlConfiguration()
        rootValues.forEach { (key, value) -> configuration.set(key, value) }
        val parent = worldsFile.toAbsolutePath().parent
        Files.createDirectories(parent)
        val temporary = Files.createTempFile(parent, "${worldsFile.fileName}.", ".tmp")
        try {
            Files.writeString(temporary, configuration.saveToString(), UTF_8)
            try {
                Files.move(
                    temporary,
                    worldsFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, worldsFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun Map<String, Any?>.toDefinition(encodedKey: String): WorldDefinition {
        val registryKey = decodeKey(encodedKey)
        val name = stringAt("read-only.legacy-world-name") ?: registryKey.substringAfter(':')
        return WorldDefinition(
            registryKey = registryKey,
            name = name,
            alias = stringAt("alias"),
            environment = stringAt("read-only.environment") ?: stringAt("environment"),
            worldType = stringAt("world-type") ?: stringAt("type"),
            seed = longAt("read-only.seed") ?: longAt("seed"),
            generator = stringAt("generator"),
            generatorSettings = stringAt("read-only.generator-settings") ?: stringAt("generator-settings"),
            generateStructures = booleanAt("generate-structures") ?: booleanAt("structures"),
            spawnLocation =
                (valueAt("spawn-location") ?: valueAt("spawnLocation"))
                    .asStringMap()
                    ?.toSpawnLocation(),
            scale = doubleAt("scale"),
            respawnWorld = stringAt("respawn-world") ?: stringAt("respawnWorld"),
            pvp = booleanAt("pvp"),
            properties = this.mapValues { (_, value) -> value.toPlainValue() },
        )
    }

    private fun applyKnownProperties(
        properties: MutableMap<String, Any?>,
        definition: WorldDefinition,
    ) {
        definition.alias?.let { properties.setPathIfChanged("alias", it) }
        definition.environment?.let {
            val path =
                when {
                    properties.containsPath("read-only.environment") -> "read-only.environment"
                    properties.containsPath("environment") -> "environment"
                    else -> "read-only.environment"
                }
            properties.setPathIfChanged(path, it)
        }
        definition.worldType?.let {
            val path =
                when {
                    properties.containsPath("world-type") -> "world-type"
                    properties.containsPath("type") -> "type"
                    else -> "world-type"
                }
            properties.setPathIfChanged(path, it)
        }
        definition.seed?.let {
            val path =
                when {
                    properties.containsPath("read-only.seed") -> "read-only.seed"
                    properties.containsPath("seed") -> "seed"
                    else -> "read-only.seed"
                }
            properties.setPathIfChanged(path, it)
        }
        definition.generator?.let { properties.setPathIfChanged("generator", it) }
        definition.generatorSettings?.let {
            val path =
                when {
                    properties.containsPath("read-only.generator-settings") -> "read-only.generator-settings"
                    properties.containsPath("generator-settings") -> "generator-settings"
                    else -> "read-only.generator-settings"
                }
            properties.setPathIfChanged(path, it)
        }
        definition.generateStructures?.let {
            val path = if (properties.containsPath("generate-structures")) "generate-structures" else "structures"
            properties.setPathIfChanged(path, it)
        }
        definition.spawnLocation?.let {
            val path = if (properties.containsPath("spawn-location")) "spawn-location" else "spawnLocation"
            properties.mergePathMap(path, it.toConfigMap())
        }
        definition.scale?.let { properties.setPathIfChanged("scale", it) }
        definition.respawnWorld?.let {
            val path = if (properties.containsPath("respawn-world")) "respawn-world" else "respawnWorld"
            properties.setPathIfChanged(path, it)
        }
        definition.pvp?.let { properties.setPathIfChanged("pvp", it) }
    }

    private fun Map<String, Any?>.stringAt(path: String): String? = valueAt(path)?.toString()

    private fun Map<String, Any?>.longAt(path: String): Long? =
        (valueAt(path) as? Number)?.toLong() ?: valueAt(path)?.toString()?.toLongOrNull()

    private fun Map<String, Any?>.doubleAt(path: String): Double? =
        (valueAt(path) as? Number)?.toDouble() ?: valueAt(path)?.toString()?.toDoubleOrNull()

    private fun Map<String, Any?>.booleanAt(path: String): Boolean? =
        (valueAt(path) as? Boolean) ?: valueAt(path)?.toString()?.toBooleanStrictOrNull()

    private fun Map<String, Any?>.valueAt(path: String): Any? {
        var current: Any? = this
        path.split('.').forEach { segment ->
            val section = current.asStringMap() ?: return null
            current = section[segment]
        }
        return current
    }

    private fun Map<String, Any?>.containsPath(path: String): Boolean {
        val parts = path.split('.')
        var current: Map<String, Any?> = this
        parts.dropLast(1).forEach { part ->
            current = current[part].asStringMap() ?: return false
        }
        return current.containsKey(parts.last())
    }

    private fun MutableMap<String, Any?>.setPathIfChanged(
        path: String,
        value: Any?,
    ) {
        val parts = path.split('.')
        var current = this
        parts.dropLast(1).forEach { part ->
            val child = current[part].asStringMap()?.toMutableMap() ?: mutableMapOf()
            current[part] = child
            current = child
        }
        if (!equivalent(current[parts.last()], value)) {
            current[parts.last()] = value
        }
    }

    private fun MutableMap<String, Any?>.mergePathMap(
        path: String,
        values: Map<String, Any?>,
    ) {
        val parts = path.split('.')
        var current = this
        parts.dropLast(1).forEach { part ->
            val child = current[part].asStringMap()?.toMutableMap() ?: mutableMapOf()
            current[part] = child
            current = child
        }
        val target = current[parts.last()].asStringMap()?.toMutableMap() ?: mutableMapOf()
        values.forEach { (key, value) ->
            if (!equivalent(target[key], value)) {
                target[key] = value
            }
        }
        current[parts.last()] = target
    }

    private fun equivalent(
        existing: Any?,
        updated: Any?,
    ): Boolean = when {
        existing is Number && updated is Number -> existing.toDouble() == updated.toDouble()
        existing is String && updated is Number ->
            existing.toDoubleOrNull()?.let { it == updated.toDouble() } ?: false
        existing is Number && updated is String ->
            updated.toDoubleOrNull()?.let { existing.toDouble() == it } ?: false
        existing is String && updated is Boolean -> existing.toBooleanStrictOrNull() == updated
        existing is Boolean && updated is String -> updated.toBooleanStrictOrNull() == existing
        else -> existing == updated
    }

    private fun Any?.asStringMap(): Map<String, Any?>? = when (this) {
        is ConfigurationSection -> getValues(false).mapValues { (_, value) -> value.toPlainValue() }
        is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toPlainValue() }
        else -> null
    }

    private fun Any?.toPlainValue(): Any? = when (this) {
        is ConfigurationSection -> getValues(false).mapValues { (_, value) -> value.toPlainValue() }
        is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toPlainValue() }
        is List<*> -> map { it.toPlainValue() }
        else -> this
    }

    private fun Map<String, Any?>.toSpawnLocation(): WorldSpawnLocation? {
        val x = (this["x"] as? Number)?.toDouble() ?: return null
        val y = (this["y"] as? Number)?.toDouble() ?: return null
        val z = (this["z"] as? Number)?.toDouble() ?: return null
        val yaw = (this["yaw"] as? Number)?.toFloat() ?: 0f
        val pitch = (this["pitch"] as? Number)?.toFloat() ?: 0f
        return WorldSpawnLocation(x, y, z, yaw, pitch)
    }

    private fun WorldSpawnLocation.toConfigMap() = mapOf(
        "x" to x,
        "y" to y,
        "z" to z,
        "yaw" to yaw,
        "pitch" to pitch,
    )

    private fun encodeKey(registryKey: String) = registryKey.replace(".", "[dot]")

    private fun decodeKey(encodedKey: String) = encodedKey.replace("[dot]", ".")
}
