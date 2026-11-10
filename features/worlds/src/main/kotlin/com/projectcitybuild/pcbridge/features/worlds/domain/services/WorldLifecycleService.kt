package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldSpawnLocation
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldDefinitionRepository
import org.bukkit.Difficulty
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.util.UUID

class WorldLifecycleService(
    private val server: Server,
    private val worldDefinitionRepository: WorldDefinitionRepository,
    private val worldBorderRepository: WorldBorderRepository,
    private val backupDirectory: Path,
) {
    fun list(): List<WorldRegistration> {
        requireMainThread()

        return worldDefinitionRepository.all()
    }

    fun importLoadedWorld(name: String): WorldDefinition {
        requireMainThread()

        val world = server.getWorld(name)
            ?: throw IllegalArgumentException("World $name is not loaded")

        val existing = worldDefinitionRepository.get(world.name)?.definition
        val definition = existing ?: WorldDefinition(
            registryKey = world.key.toString(),
            name = world.name,
            alias = "",
            environment = world.environment.name,
            worldType = world.worldType?.name,
            seed = world.seed,
            generator = null,
            generatorSettings = null,
            generateStructures = null,
            spawnLocation = world.spawnLocation.let {
                WorldSpawnLocation(it.x, it.y, it.z, it.yaw, it.pitch)
            },
            scale = world.coordinateScale,
            respawnWorld = null,
            pvp = world.pvp,
            properties = mapOf("difficulty" to world.difficulty.name),
        )
        worldDefinitionRepository.save(definition)
        return definition
    }

    fun load(name: String): World {
        requireMainThread()

        server.getWorld(name)?.let { return it }

        val definition = worldDefinitionRepository.get(name)?.definition
            ?: throw IllegalArgumentException("World $name is not registered")

        val folder = worldFolder(definition.name)
        requireWorldData(folder, definition.name)

        return createWorld(definition)
    }

    fun create(definition: WorldDefinition): World {
        requireMainThread()

        requireSafeWorldName(definition.name)
        check(server.getWorld(definition.name) == null) { "World ${definition.name} is already loaded" }
        val folder = worldFolder(definition.name)
        check(!Files.exists(folder, LinkOption.NOFOLLOW_LINKS)) {
            "World data for ${definition.name} already exists; import it instead"
        }
        check(worldDefinitionRepository.get(definition.registryKey) == null) {
            "World ${definition.registryKey} is already registered"
        }
        return try {
            createWorld(definition).also {
                worldDefinitionRepository.save(definition)
            }
        } catch (failure: Throwable) {
            rollbackWorldCreation(definition.name, folder, failure)
            throw failure
        }
    }

    fun unload(name: String) {
        requireMainThread()

        val world = server.getWorld(name) ?: throw IllegalArgumentException("World $name is not loaded")
        check(world.players.isEmpty()) {
            "World $name contains players and cannot be unloaded"
        }
        world.save()

        check(server.unloadWorld(world, true)) {
            "Paper refused to unload world $name"
        }
        worldBorderRepository.unload(world)
    }

    fun clone(
        sourceName: String,
        targetName: String,
        confirmed: Boolean,
    ): World {
        requireMainThread()
        requireSafeWorldName(targetName)

        check(confirmed) {
            "Cloning a world requires explicit confirmation"
        }
        check(server.getWorld(sourceName) == null) {
            "Unload source world $sourceName before cloning it"
        }
        check(server.getWorld(targetName) == null) {
            "World $targetName is already loaded"
        }
        check(worldDefinitionRepository.get(targetName) == null) {
            "World $targetName is already registered"
        }

        val sourceDefinition = worldDefinitionRepository.get(sourceName)?.definition
            ?: throw IllegalArgumentException("World $sourceName is not registered")

        val sourceFolder = worldFolder(sourceName)
        val targetFolder = worldFolder(targetName)
        check(Files.isDirectory(sourceFolder)) {
            "World folder for $sourceName does not exist"
        }
        requireWorldData(sourceFolder, sourceName)
        check(!Files.exists(targetFolder, LinkOption.NOFOLLOW_LINKS)) {
            "World folder for $targetName already exists"
        }

        val targetKey = sourceDefinition.registryKey
            .substringBeforeLast(':', missingDelimiterValue = "")
            .takeIf(String::isNotEmpty)
            ?.let { "$it:$targetName" }
            ?: targetName

        val definition = sourceDefinition.copy(
            registryKey = targetKey,
            name = targetName,
            properties = sourceDefinition.properties.withLegacyName(targetName),
        )
        return try {
            copyDirectory(sourceFolder, targetFolder, excludeTransientFiles = true)
            createWorld(definition).also {
                worldDefinitionRepository.save(definition)
            }
        } catch (failure: Throwable) {
            rollbackWorldCreation(targetName, targetFolder, failure)
            throw failure
        }
    }

    fun regenerate(
        name: String,
        confirmed: Boolean,
    ): World {
        requireMainThread()

        check(confirmed) {
            "Regenerating a world requires explicit confirmation"
        }

        val registration = worldDefinitionRepository.get(name)
            ?: throw IllegalArgumentException("World $name is not registered")

        val definition = registration.definition
        val loadedWorld = server.getWorld(definition.name)
        check(loadedWorld == null || loadedWorld.players.isEmpty()) {
            "World ${definition.name} contains players and cannot be regenerated"
        }

        val folder = worldFolder(definition.name)
        check(
            loadedWorld == null || loadedWorld.worldFolder
                .toPath()
                .toAbsolutePath()
                .normalize() == folder,
        ) {
            "World folder for ${definition.name} is outside the server world container"
        }
        check(Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS)) {
            "World folder for ${definition.name} does not exist"
        }
        loadedWorld?.save()
        val backup = backupWorldFolder(folder, definition.name)

        return try {
            if (loadedWorld != null) {
                check(server.unloadWorld(loadedWorld, false)) {
                    "Paper refused to unload world ${definition.name}"
                }
                worldBorderRepository.unload(loadedWorld)
            }
            deleteDirectory(folder)
            createWorld(definition)
        } catch (failure: Throwable) {
            val partiallyLoaded = server.getWorld(definition.name)
            if (partiallyLoaded != null && !server.unloadWorld(partiallyLoaded, false)) {
                failure.addSuppressed(
                    IllegalStateException(
                        "Could not unload regenerated world ${definition.name}; backup remains at $backup",
                    ),
                )
                throw failure
            }
            partiallyLoaded?.let(worldBorderRepository::unload)
            try {
                if (Files.exists(folder)) deleteDirectory(folder)
                copyDirectory(backup, folder, excludeTransientFiles = false)
            } catch (restoreFailure: Throwable) {
                failure.addSuppressed(
                    IllegalStateException("Could not restore ${definition.name}; backup remains at $backup", restoreFailure),
                )
            }
            throw failure
        }
    }

    fun unregister(name: String) {
        requireMainThread()

        val registration = worldDefinitionRepository.get(name)
            ?: throw IllegalArgumentException("World $name is not registered")

        check(server.getWorld(registration.definition.name) == null) {
            "Unload world ${registration.definition.name} before unregistering it"
        }
        check(worldDefinitionRepository.unregister(registration.definition.registryKey)) {
            "World ${registration.definition.name} could not be unregistered"
        }
    }

    fun delete(
        name: String,
        confirmed: Boolean,
    ) {
        requireMainThread()
        check(confirmed) { "Deleting a world requires explicit confirmation" }
        val registration =
            worldDefinitionRepository.get(name)
                ?: throw IllegalArgumentException("World $name is not registered")
        val definition = registration.definition
        check(server.getWorld(definition.name) == null) { "Unload world ${definition.name} before deleting it" }
        val folder = worldFolder(definition.name)
        check(Files.isDirectory(folder)) { "World folder for ${definition.name} does not exist" }
        val backup = backupWorldFolder(folder, definition.name)
        try {
            deleteDirectory(folder)
            check(worldDefinitionRepository.unregister(definition.registryKey)) {
                "World data was deleted, but registration could not be removed"
            }
        } catch (failure: Throwable) {
            try {
                if (Files.exists(folder)) deleteDirectory(folder)
                copyDirectory(backup, folder, excludeTransientFiles = false)
            } catch (restoreFailure: Throwable) {
                failure.addSuppressed(restoreFailure)
            }
            throw failure
        }
    }

    private fun createWorld(definition: WorldDefinition): World {
        val environment = definition.environment?.let {
            runCatching { World.Environment.valueOf(it.uppercase()) }
                .getOrElse { failure -> throw IllegalArgumentException("Invalid world environment: $it", failure) }
        } ?: World.Environment.NORMAL

        val type = definition.worldType?.let {
            runCatching { WorldType.valueOf(it.uppercase()) }
                .getOrElse { failure -> throw IllegalArgumentException("Invalid world type: $it", failure) }
        } ?: WorldType.NORMAL

        val creator = WorldCreator(definition.name)
            .environment(environment)
            .type(type)

        definition.generatorSettings?.let(creator::generatorSettings)
        definition.seed?.let(creator::seed)
        definition.generator?.let(creator::generator)
        definition.generateStructures?.let(creator::generateStructures)
        val world = server.createWorld(creator) ?: throw IllegalStateException("Paper failed to load ${definition.name}")

        definition.pvp?.let(world::setPVP)
        definition.properties["difficulty"]?.toString()?.let {
            runCatching { Difficulty.valueOf(it.uppercase()) }.getOrNull()?.let(world::setDifficulty)
        }
        definition.spawnLocation?.let { spawn ->
            world.setSpawnLocation(spawn.x.toInt(), spawn.y.toInt(), spawn.z.toInt(), spawn.yaw)
        }
        return world
    }

    private fun worldFolder(name: String): Path {
        requireSafeWorldName(name)

        val root = server.worldContainer
            .toPath()
            .toAbsolutePath()
            .normalize()

        val path = root.resolve(name).normalize()

        check(path.parent == root) {
            "World name resolves outside the server world container"
        }
        check(!Files.isSymbolicLink(path)) {
            "Symbolic links are not supported for world folders"
        }
        return path
    }

    private fun requireWorldData(
        folder: Path,
        name: String,
    ) {
        check(Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS)) {
            "World folder for $name does not exist; refusing to create a replacement"
        }
        check(
            listOf("level.dat", "level.dat_old").any { file ->
                Files.isRegularFile(folder.resolve(file), LinkOption.NOFOLLOW_LINKS)
            },
        ) {
            "World data for $name is missing level.dat; refusing to create a replacement"
        }
    }

    private fun backupWorldFolder(
        folder: Path,
        name: String,
    ): Path {
        val normalizedFolder = folder.toAbsolutePath().normalize()
        val normalizedBackupDirectory = backupDirectory.toAbsolutePath().normalize()
        check(!normalizedBackupDirectory.startsWith(normalizedFolder)) {
            "World backup directory must not be inside the world folder"
        }
        Files.createDirectories(backupDirectory)
        val realFolder = folder.toRealPath()
        val realBackupDirectory = backupDirectory.toRealPath()
        check(!realBackupDirectory.startsWith(realFolder)) {
            "World backup directory must not resolve inside the world folder"
        }
        val backup = realBackupDirectory.resolve("$name-${Instant.now().toEpochMilli()}-${UUID.randomUUID()}")
        copyDirectory(folder, backup, excludeTransientFiles = false)
        return backup
    }

    private fun rollbackWorldCreation(
        name: String,
        folder: Path,
        failure: Throwable,
    ) {
        try {
            server.getWorld(name)?.let { world ->
                check(server.unloadWorld(world, true)) { "Could not unload incomplete world $name" }
                worldBorderRepository.unload(world)
            }
        } catch (cleanupFailure: Throwable) {
            failure.addSuppressed(cleanupFailure)
        }
        if (server.getWorld(name) == null) {
            try {
                if (Files.exists(folder)) deleteDirectory(folder)
            } catch (cleanupFailure: Throwable) {
                failure.addSuppressed(cleanupFailure)
            }
        }
    }

    private fun copyDirectory(
        source: Path,
        destination: Path,
        excludeTransientFiles: Boolean,
    ) {
        Files.walkFileTree(
            source,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    check(!Files.isSymbolicLink(dir)) { "Symbolic links are not supported in world folders" }
                    Files.createDirectories(destination.resolve(source.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    check(!Files.isSymbolicLink(file)) { "Symbolic links are not supported in world folders" }
                    if (excludeTransientFiles && file.fileName.toString() in setOf("uid.dat", "session.lock")) {
                        return FileVisitResult.CONTINUE
                    }
                    val target = destination.resolve(source.relativize(file))
                    Files.createDirectories(target.parent)
                    Files.copy(file, target)
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private fun deleteDirectory(directory: Path) {
        if (!Files.exists(directory)) return
        Files.walkFileTree(
            directory,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(
                    dir: Path,
                    failure: IOException?,
                ): FileVisitResult {
                    failure?.let { throw it }
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private fun requireMainThread() {
        check(server.isPrimaryThread) { "World lifecycle operations must run on the server thread" }
    }

    private fun requireSafeWorldName(name: String) {
        require(name.isNotBlank() && name != "." && name != ".." && name.none(Char::isISOControl)) {
            "World name is invalid"
        }
        require('/' !in name && '\\' !in name) { "World name must not contain path separators" }
    }

    private fun Map<String, Any?>.withLegacyName(name: String): Map<String, Any?> {
        val result = toMutableMap()
        val readOnly = result["read-only"] as? Map<*, *>
        if (readOnly != null) {
            result["read-only"] =
                readOnly.entries
                    .associate { (key, value) -> key.toString() to value }
                    .toMutableMap()
                    .apply { put("legacy-world-name", name) }
        }
        return result
    }
}
