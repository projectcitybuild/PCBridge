package com.projectcitybuild.pcbridge.paper.features.borders.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import com.projectcitybuild.pcbridge.paper.features.borders.data.Border
import org.bukkit.World
import java.io.File
import java.util.UUID

class WorldBorderRepository(
    private val storage: JsonStorage<Border>,
) {
    data class CachedBorder(
        val border: Border?,
    )

    private val cache: MutableMap<UUID, CachedBorder> = mutableMapOf()

    suspend fun get(world: World): Border? {
        val cached = cache[world.uid]
        if (cached != null) {
            return cached.border
        }
        val border = storage.read(world.borderMetaFile())
        cache[world.uid] = CachedBorder(border)
        return border
    }

    suspend fun set(world: World, border: Border) {
        storage.write(
            file = world.borderMetaFile(),
            data = border,
        )
        cache[world.uid] = CachedBorder(border)
    }

    suspend fun delete(world: World) {
        storage.delete(world.borderMetaFile())
        cache[world.uid] = CachedBorder(null)
    }
}

private fun World.borderMetaFile(): File {
    return File(worldFolder, "meta/border.json")
}