package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import org.bukkit.Server
import org.bukkit.World
import java.io.File
import java.util.UUID

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
        world.worldBorder.setCenter(validated.centerX, validated.centerZ)
        world.worldBorder.size = validated.nativeBorderSize
        server.onlinePlayers
            .filter { it.world.uid == world.uid && !validated.contains(it.location.x, it.location.z) }
            .forEach { it.teleport(validated.clamp(it.location)) }
    }

    fun load(world: World): SerializableWorldBorder? {
        val border = get(world) ?: return null
        world.worldBorder.setCenter(border.centerX, border.centerZ)
        world.worldBorder.size = border.nativeBorderSize
        return border
    }

    fun loadAllLoaded() {
        server.worlds.forEach(::load)
    }

    fun allLoaded(): List<Pair<World, SerializableWorldBorder>> =
        server.worlds.mapNotNull { world ->
            get(world)?.let { world to it }
        }
}

private data class CachedBorder(
    val border: SerializableWorldBorder?,
)

private fun World.borderMetaFile(): File = File(worldFolder, "meta/border.json")
