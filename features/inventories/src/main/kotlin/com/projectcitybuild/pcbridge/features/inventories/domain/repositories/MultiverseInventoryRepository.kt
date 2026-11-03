package com.projectcitybuild.pcbridge.features.inventories.domain.repositories

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryCompatibilityMode
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryCompatibilityReport
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryGroupConflict
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileLocation
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileType
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryWorldGroup
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.data.deepCopyObject
import com.projectcitybuild.pcbridge.features.inventories.domain.data.requireSafePathSegment
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MultiverseInventoryRepository(
    private val dataDirectory: Path,
    private val backupDirectory: Path,
    private val mode: InventoryCompatibilityMode,
    private val multiverseInventoriesEnabled: () -> Boolean,
) {
    private val fileLocks = ConcurrentHashMap<Path, Any>()
    private val backupLock = Any()

    @Volatile
    private var backupPath: Path? = null

    fun inspect(): InventoryCompatibilityReport {
        val malformed = mutableListOf<File>()
        val malformedYaml = mutableListOf<File>()
        val worldProfiles = countProfiles("worlds", malformed)
        val groupProfiles = countProfiles("groups", malformed)
        val globalProfiles = countJsonFiles("players", malformed)
        val playerNames = dataDirectory.resolve("playernames.json")

        if (Files.exists(playerNames)) {
            recordInvalidJson(playerNames, malformed)
        }
        listOf("config.yml", "groups.yml")
            .map(dataDirectory::resolve)
            .filter(Files::isRegularFile)
            .forEach { path -> recordInvalidYaml(path, malformedYaml) }

        return InventoryCompatibilityReport(
            groupConfigurationPresent = Files.isRegularFile(dataDirectory.resolve("groups.yml")),
            inventoryConfigurationPresent = Files.isRegularFile(dataDirectory.resolve("config.yml")),
            playerNameMapPresent = Files.isRegularFile(playerNames),
            worldProfiles = worldProfiles,
            groupProfiles = groupProfiles,
            globalProfiles = globalProfiles,
            malformedJsonFiles = malformed,
            malformedYamlFiles = malformedYaml,
        )
    }

    fun readProfile(location: InventoryProfileLocation): JsonObject? = synchronized(lockFor(profilePath(location))) {
        readJsonObject(profilePath(location))
    }

    fun updateProfile(
        location: InventoryProfileLocation,
        profileType: InventoryProfileType,
        fields: Map<String, com.google.gson.JsonElement>,
        statsFields: Map<String, com.google.gson.JsonElement> = emptyMap(),
    ) {
        val path = profilePath(location)
        synchronized(lockFor(path)) {
            requireWriteAccess()
            val document = readJsonObject(path) ?: JsonObject()
            val profile = document.get(profileType.name)?.let {
                require(it.isJsonObject) { "Profile type ${profileType.name} is not a JSON object in $path" }
                it.asJsonObject.deepCopyObject()
            } ?: JsonObject()

            fields.forEach { (key, value) ->
                require(key.isNotBlank()) { "Profile field names must not be blank" }
                profile.add(key, value.deepCopy())
            }
            if (statsFields.isNotEmpty()) {
                val stats =
                    profile.get("stats")?.let {
                        require(it.isJsonObject) { "Profile stats are not a JSON object in $path" }
                        it.asJsonObject.deepCopyObject()
                    } ?: JsonObject()
                statsFields.forEach { (key, value) ->
                    require(key.isNotBlank()) { "Profile stat names must not be blank" }
                    stats.add(key, value.deepCopy())
                }
                profile.add("stats", stats)
            }
            document.add(profileType.name, profile)
            atomicWrite(path, document.toString())
        }
    }

    fun profileName(
        playerId: UUID,
        currentName: String,
    ): String {
        requireSafePathSegment(currentName, "player name")
        val storedName = readPlayerNames()?.get(playerId.toString())
        return storedName
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
            ?.also { requireSafePathSegment(it, "stored player name") }
            ?: currentName
    }

    fun readGlobalProfile(playerId: UUID): JsonObject? = synchronized(lockFor(globalProfilePath(playerId))) {
        readJsonObject(globalProfilePath(playerId))
    }

    fun updateGlobalPlayerData(
        playerId: UUID,
        fields: Map<String, com.google.gson.JsonElement>,
    ) {
        val path = globalProfilePath(playerId)
        synchronized(lockFor(path)) {
            requireWriteAccess()
            val document = readJsonObject(path) ?: JsonObject()
            val playerData =
                document.get("playerData")?.let {
                    require(it.isJsonObject) { "playerData is not a JSON object in $path" }
                    it.asJsonObject.deepCopyObject()
                } ?: JsonObject()
            fields.forEach { (key, value) ->
                requireSafePathSegment(key, "global profile field")
                playerData.add(key, value.deepCopy())
            }
            document.add("playerData", playerData)
            atomicWrite(path, document.toString())
        }
    }

    fun readPlayerNames(): JsonObject? {
        val path = dataDirectory.resolve("playernames.json")
        return synchronized(lockFor(path)) {
            readJsonObject(path)
        }
    }

    fun readConfiguration(): MultiverseInventoryConfig {
        return MultiverseInventoryConfig.load(dataDirectory.resolve("config.yml"))
    }

    fun readWorldGroups(): List<InventoryWorldGroup> {
        val groupsFile = dataDirectory.resolve("groups.yml")
        if (!Files.isRegularFile(groupsFile)) return emptyList()
        val configuration = YamlConfiguration()
        configuration.load(groupsFile.toFile())
        val groups = configuration.getConfigurationSection("groups") ?: return emptyList()
        return groups.getKeys(false).mapNotNull { name ->
            val section = groups.getConfigurationSection(name) ?: return@mapNotNull null
            InventoryWorldGroup(
                name = name,
                worlds = section.getStringList("worlds").toSet(),
                shares = section.getStringList("shares").map(String::lowercase).toSet(),
                disabledShares = section.getStringList("disabled-shares").map(String::lowercase).toSet(),
                spawnWorld = section.getString("spawn.world"),
                spawnPriority = section.getInt("spawn.priority").takeIf { section.contains("spawn.priority") },
            )
        }
    }

    fun findGroupShareConflicts(): List<InventoryGroupConflict> {
        val worldGroups =
            readWorldGroups()
                .flatMap { group ->
                    (group.shares - group.disabledShares).map { share -> group to share }
                        .flatMap { (group, share) -> group.worlds.map { world -> Triple(world, share, group.name) } }
                }
                .groupBy { it.first to it.second }
        return worldGroups.mapNotNull { (worldAndShare, memberships) ->
            val groups = memberships.map { it.third }.toSet()
            if (groups.size < 2) {
                null
            } else {
                InventoryGroupConflict(
                    world = worldAndShare.first,
                    share = worldAndShare.second,
                    groups = groups,
                )
            }
        }
    }

    private fun requireWriteAccess() {
        check(mode == InventoryCompatibilityMode.ACTIVE) {
            "Multiverse-Inventories data is in read-only validation mode"
        }
        check(!multiverseInventoriesEnabled()) {
            "Disable Multiverse-Inventories before enabling PCBridge inventory writes"
        }
        ensureBackup()
    }

    private fun ensureBackup() {
        if (backupPath != null) return
        synchronized(backupLock) {
            if (backupPath != null) return
            Files.createDirectories(backupDirectory)
            val destination =
                backupDirectory.resolve(
                    "Multiverse-Inventories-${Instant.now().toEpochMilli()}-${UUID.randomUUID()}",
                )
            try {
                if (Files.exists(dataDirectory)) {
                    copyTree(dataDirectory, destination)
                } else {
                    Files.createDirectories(destination)
                }
            } catch (failure: Throwable) {
                try {
                    deleteTree(destination)
                } catch (cleanupFailure: Throwable) {
                    failure.addSuppressed(cleanupFailure)
                }
                throw failure
            }
            backupPath = destination
        }
    }

    private fun profilePath(location: InventoryProfileLocation): Path = dataDirectory.resolve(location.directory)
        .resolve(location.key)
        .resolve("${location.playerName}.json")

    private fun globalProfilePath(playerId: UUID): Path = dataDirectory.resolve("players")
        .resolve("$playerId.json")

    private fun countProfiles(
        directory: String,
        malformed: MutableList<File>,
    ): Int {
        val root = dataDirectory.resolve(directory)
        if (!Files.isDirectory(root)) return 0
        return Files.walk(root).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
                .peek { path -> recordInvalidJson(path, malformed) }
                .count()
                .toInt()
        }
    }

    private fun countJsonFiles(
        directory: String,
        malformed: MutableList<File>,
    ): Int {
        val root = dataDirectory.resolve(directory)
        if (!Files.isDirectory(root)) return 0
        return Files.list(root).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
                .peek { path -> recordInvalidJson(path, malformed) }
                .count()
                .toInt()
        }
    }

    private fun recordInvalidJson(
        path: Path,
        malformed: MutableList<File>,
    ) {
        try {
            require(parseJson(path).isJsonObject) { "Expected a JSON object in $path" }
        } catch (_: RuntimeException) {
            malformed.add(path.toFile())
        }
    }

    private fun recordInvalidYaml(
        path: Path,
        malformed: MutableList<File>,
    ) {
        try {
            YamlConfiguration().load(path.toFile())
        } catch (_: InvalidConfigurationException) {
            malformed.add(path.toFile())
        }
    }

    private fun readJsonObject(path: Path): JsonObject? {
        if (!Files.isRegularFile(path)) return null
        val json = parseJson(path)
        require(json.isJsonObject) { "Expected a JSON object in $path" }
        return json.asJsonObject
    }

    private fun parseJson(path: Path) = JsonParser.parseString(Files.readString(path, UTF_8))

    private fun atomicWrite(
        path: Path,
        contents: String,
    ) {
        val parent = path.parent
        Files.createDirectories(parent)
        val temporary = Files.createTempFile(parent, "${path.fileName}.", ".tmp")
        try {
            Files.writeString(temporary, contents, UTF_8)
            try {
                Files.move(
                    temporary,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun copyTree(
        source: Path,
        destination: Path,
    ) {
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                check(!Files.isSymbolicLink(path)) {
                    "Multiverse-Inventories backups do not support symbolic links: $path"
                }
                val target = destination.resolve(source.relativize(path).toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES)
                }
            }
        }
    }

    private fun deleteTree(directory: Path) {
        if (!Files.exists(directory)) return
        Files.walk(directory).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::delete)
        }
    }

    private fun lockFor(path: Path): Any = fileLocks.computeIfAbsent(path.toAbsolutePath().normalize()) { Any() }
}
