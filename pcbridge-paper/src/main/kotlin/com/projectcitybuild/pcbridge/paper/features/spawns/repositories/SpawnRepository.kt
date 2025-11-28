package com.projectcitybuild.pcbridge.paper.features.spawns.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import com.projectcitybuild.pcbridge.paper.features.spawns.data.SerializableSpawn
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsTracer
import kotlinx.coroutines.coroutineScope
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import java.io.File
import java.util.UUID

class SpawnRepository(
    private val server: Server,
    private val storage: Storage<SerializableSpawn>,
) {
    private val cache: MutableMap<UUID, Location> = mutableMapOf()

    suspend fun get(
        world: World,
    ): Location = spawnsTracer.trace("SpawnRepository.get") {
        val cached = cache[world.uid]
        if (cached != null) {
            return@trace cached
        }
        val serialized = storage.read(world.spawnMetaFile())
        if (serialized != null) {
            val location = serialized.toLocation(world)
            cache[world.uid] = location
            return@trace location
        }
        // Fallback to world spawn without yaw and pitch
        return@trace world.spawnLocation
    }

    suspend fun set(
        location: Location,
    ) = spawnsTracer.trace("SpawnRepository.set") {
        val world = location.world

        world.setSpawnLocation(location)

        // Minecraft API does not save yaw and pitch, so we unfortunately need to
        // handle this ourselves. We'll store the data in a folder nested inside the
        // world's folder to keep it synced (i.e. make clean up unnecessary)
        storage.write(
            file = world.spawnMetaFile(),
            data = SerializableSpawn.fromLocation(location),
        )
        cache[world.uid] = location
    }

    suspend fun allLoaded(): List<Location> = spawnsTracer.trace("SpawnRepository.all") {
        // TODO: is this concurrent?
        return@trace coroutineScope {
            server.worlds.map { get(it) }
        }
    }
}

private fun World.spawnMetaFile(): File {
    return File(worldFolder, "meta/spawn.json")
}