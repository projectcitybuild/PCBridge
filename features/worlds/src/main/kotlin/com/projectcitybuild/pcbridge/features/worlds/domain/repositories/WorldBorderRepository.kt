package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import org.bukkit.Server
import org.bukkit.World
import java.io.File
import java.nio.file.Files
import java.util.UUID

private const val MAX_WORLD_BORDER_SIZE = SerializableWorldBorder.MAX_DIMENSION

class WorldBorderRepository(
    private val server: Server,
    private val fileStorage: FileStorage<SerializableWorldBorder>,
) {
    private val cache = mutableMapOf<UUID, CachedBorder>()

    fun get(world: World): SerializableWorldBorder? {
        cache[world.uid]?.let { return it.border }

        val border = fileStorage.readSync(world.borderMetaFile())?.validate()
        cache[world.uid] = CachedBorder(border)
        return border
    }

    suspend fun set(
        world: World,
        border: SerializableWorldBorder,
    ) {
        val validated = border.validate()
        fileStorage.write(world.borderMetaFile(), validated)
        cache[world.uid] = CachedBorder(validated)
        world.applyNativeBorder(validated)
        server.onlinePlayers
            .filter { it.world.uid == world.uid && !validated.contains(it.location.x, it.location.z) }
            .forEach { it.teleport(validated.clamp(it.location)) }
    }

    fun clear(world: World) {
        Files.deleteIfExists(world.borderMetaFile().toPath())
        cache[world.uid] = CachedBorder(null)
        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = MAX_WORLD_BORDER_SIZE
    }

    fun load(world: World): SerializableWorldBorder? {
        val border = get(world) ?: return null
        world.applyNativeBorder(border)
        return border
    }

    fun loadAllLoaded() {
        server.worlds.forEach(::load)
    }

    fun allLoaded(): List<Pair<World, SerializableWorldBorder>> = server.worlds.mapNotNull { world ->
        get(world)?.let { world to it }
    }
}

private data class CachedBorder(
    val border: SerializableWorldBorder?,
)

private fun World.applyNativeBorder(border: SerializableWorldBorder) {
    if (border.effectiveShape == WorldBorderShape.SQUARE) {
        worldBorder.setCenter(border.centerX, border.centerZ)
        worldBorder.size = border.nativeBorderSize
    } else {
        worldBorder.setCenter(0.0, 0.0)
        worldBorder.size = MAX_WORLD_BORDER_SIZE
    }
}

private fun World.borderMetaFile() = File(worldFolder, "meta/border.json")
