package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.bridging.toSerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.bridging.toWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry
import org.bukkit.Server
import org.bukkit.World
import java.io.File
import java.nio.file.Files
import java.util.UUID

private const val MAX_WORLD_BORDER_SIZE = WorldBorderGeometry.MAX_DIMENSION

class WorldBorderRepository(
    private val server: Server,
    private val fileStorage: FileStorage<SerializableWorldBorder>,
) {
    private val cache = mutableMapOf<UUID, CachedBorder>()

    fun get(world: World): WorldBorder? {
        cache[world.uid]?.let { return it.border }

        val border = fileStorage.readSync(world.borderMetaFile())?.toWorldBorder()
        cache[world.uid] = CachedBorder(border)
        return border
    }

    suspend fun set(
        world: World,
        border: WorldBorder,
    ) {
        val validated = WorldBorderGeometry(border).validate()
        fileStorage.write(world.borderMetaFile(), validated.toSerializableWorldBorder())
        cache[world.uid] = CachedBorder(validated)
        world.applyNativeBorder(validated)
        val geometry = WorldBorderGeometry(validated)
        server.onlinePlayers
            .filter { it.world.uid == world.uid && !geometry.contains(it.location.x, it.location.z) }
            .forEach { it.teleport(geometry.clamp(it.location)) }
    }

    fun clear(world: World) {
        Files.deleteIfExists(world.borderMetaFile().toPath())
        cache[world.uid] = CachedBorder(null)
        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = MAX_WORLD_BORDER_SIZE
    }

    fun load(world: World): WorldBorder? {
        val border = get(world) ?: return null
        world.applyNativeBorder(border)
        return border
    }

    fun loadAllLoaded() {
        server.worlds.forEach(::load)
    }

    fun allLoaded(): List<Pair<World, WorldBorder>> = server.worlds.mapNotNull { world ->
        get(world)?.let { world to it }
    }
}

private data class CachedBorder(
    val border: WorldBorder?,
)

private fun World.applyNativeBorder(border: WorldBorder) {
    if (border is WorldBorder.Square) {
        worldBorder.setCenter(border.centerX, border.centerZ)
        worldBorder.size = WorldBorderGeometry(border).nativeBorderSize
    } else {
        worldBorder.setCenter(0.0, 0.0)
        worldBorder.size = MAX_WORLD_BORDER_SIZE
    }
}

private fun World.borderMetaFile() = File(worldFolder, "meta/border.json")
