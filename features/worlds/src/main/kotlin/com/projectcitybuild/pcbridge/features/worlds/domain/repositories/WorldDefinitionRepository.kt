package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldSpawnLocation
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.withLock

class WorldDefinitionRepository(
    private val worldsFile: Path,
    private val isWorldLoaded: (String) -> Boolean,
    private val logger: Logger = Logger.getLogger(WorldDefinitionRepository::class.java.name),
) {
    private val lock = ReentrantLock()

    fun all(): List<WorldRegistration> = lock.withLock {
        load().worldEntries().mapNotNull { (encodedKey, raw) ->
            try {
                val properties = raw.asStringMap()
                    ?: throw IllegalArgumentException("Registry entry must be a YAML mapping")
                val definition = properties.toDefinition(encodedKey)
                WorldRegistration(
                    definition = definition,
                    isLoaded = isWorldLoaded(definition.name),
                )
            } catch (failure: Exception) {
                logger.log(
                    Level.WARNING,
                    "Skipping invalid world registry entry '$encodedKey' in $worldsFile",
                    failure,
                )
                null
            }
        }
    }

    fun get(registryKey: String): WorldRegistration? = all().firstOrNull {
        it.definition.registryKey == registryKey ||
            it.definition.name == registryKey
    }

    fun save(definition: WorldDefinition) {
        require(definition.registryKey.isNotBlank()) {
            "World registry key must not be blank"
        }
        require(definition.name.isNotBlank()) {
            "World name must not be blank"
        }
        lock.withLock {
            val rootValues = load().mapValues { (_, value) -> value.toPlainValue() }.toMutableMap()
            val worlds = rootValues.worldEntries().toMutableMap()
            val properties = definition.properties.toMutableMap()
            applyKnownProperties(properties, definition)
            worlds[encodeKey(definition.registryKey)] = properties
            rootValues["worlds"] = worlds
            write(rootValues)
        }
    }

    fun unregister(registryKey: String): Boolean {
        return lock.withLock {
            val encodedKey = encodeKey(registryKey)
            val rootValues = load().toMutableMap()
            val worlds = rootValues.worldEntries().toMutableMap()
            if (!worlds.containsKey(encodedKey)) return false

            worlds.remove(encodedKey)
            rootValues["worlds"] = worlds
            write(rootValues)
            true
        }
    }

    private fun load(): Map<String, Any?> {
        if (!Files.exists(worldsFile)) {
            return emptyMap()
        }
        val yaml = Yaml(SafeConstructor(LoaderOptions()))
        val loaded = yaml.load<Any?>(Files.readString(worldsFile))
            ?: return emptyMap()

        return loaded.asStringMap()
            ?: throw IllegalArgumentException("World registry root must be a YAML mapping: $worldsFile")
    }

    private fun Map<String, Any?>.worldEntries(): Map<String, Any?> {
        if (!containsKey("worlds")) return emptyMap()

        return this["worlds"].asStringMap()
            ?: throw IllegalArgumentException("World registry 'worlds' section must be a YAML mapping: $worldsFile")
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
            generator = stringAt("generator")
                ?.trim()
                ?.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) },
            generatorSettings = stringAt("read-only.generator-settings") ?: stringAt("generator-settings"),
            generateStructures = booleanAt("generate-structures") ?: booleanAt("structures"),
            spawnLocation = (valueAt("spawn-location") ?: valueAt("spawnLocation"))
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
            val path = when {
                properties.containsPath("read-only.environment") -> "read-only.environment"
                properties.containsPath("environment") -> "environment"
                else -> "read-only.environment"
            }
            properties.setPathIfChanged(path, it)
        }
        definition.worldType?.let {
            val path = when {
                properties.containsPath("world-type") -> "world-type"
                properties.containsPath("type") -> "type"
                else -> "world-type"
            }
            properties.setPathIfChanged(path, it)
        }
        definition.seed?.let {
            val path = when {
                properties.containsPath("read-only.seed") -> "read-only.seed"
                properties.containsPath("seed") -> "seed"
                else -> "read-only.seed"
            }
            properties.setPathIfChanged(path, it)
        }
        definition.generator?.let { properties.setPathIfChanged("generator", it) }
        definition.generatorSettings?.let {
            val path = when {
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
        existing is Number && updated is Number -> {
            existing.toDouble() == updated.toDouble()
        }
        existing is String && updated is Number -> {
            existing.toDoubleOrNull()?.let { it == updated.toDouble() } ?: false
        }
        existing is Number && updated is String -> {
            updated.toDoubleOrNull()?.let { existing.toDouble() == it } ?: false
        }
        existing is String && updated is Boolean -> {
            existing.toBooleanStrictOrNull() == updated
        }
        existing is Boolean && updated is String -> {
            updated.toBooleanStrictOrNull() == existing
        }
        else -> {
            existing == updated
        }
    }

    private fun Any?.asStringMap(): Map<String, Any?>? = when (this) {
        is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toPlainValue() }
        else -> null
    }

    private fun Any?.toPlainValue(): Any? = when (this) {
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
