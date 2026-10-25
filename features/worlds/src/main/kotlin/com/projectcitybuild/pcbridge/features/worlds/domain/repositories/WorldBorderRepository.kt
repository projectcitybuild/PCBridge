package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.worlds.domain.bridging.toSerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.bridging.toWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.bukkit.World
import java.io.File
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WorldBorderRepository(
    private val server: Server,
    private val fileStorage: FileStorage<SerializableWorldBorder>,
) {
    private val cache = ConcurrentHashMap<UUID, CachedBorder>()

    fun get(world: World): WorldBorder? = cache.computeIfAbsent(world.uid) {
        val border = try {
            fileStorage.readSync(world.borderMetaFile())?.toWorldBorder()
        } catch (failure: Exception) {
            logSync.error(failure) { "Failed to read world border for ${world.name}" }
            null
        }
        CachedBorder(border)
    }.border

    suspend fun set(world: World, border: WorldBorder) {
        fileStorage.write(world.borderMetaFile(), border.toSerializableWorldBorder())
        cache[world.uid] = CachedBorder(border)
    }

    suspend fun clear(world: World) {
        val borderFile = world.borderMetaFile()
        withContext(Dispatchers.IO) {
            Files.deleteIfExists(borderFile.toPath())
        }
        cache[world.uid] = CachedBorder(null)
    }

    fun unload(world: World) {
        cache.remove(world.uid)
    }

    fun allLoaded(): List<Pair<World, WorldBorder>> = server.worlds.mapNotNull { world ->
        get(world)?.let { world to it }
    }
}

private data class CachedBorder(
    val border: WorldBorder?,
)

private fun World.borderMetaFile() = File(worldFolder, "meta/border.json")
