package com.projectcitybuild.pcbridge.paper.features.builds.repositories

import com.projectcitybuild.pcbridge.http.models.pcb.Build
import com.projectcitybuild.pcbridge.http.models.pcb.PaginatedResponse
import com.projectcitybuild.pcbridge.http.services.pcb.BuildHttpService
import org.bukkit.Location
import org.bukkit.entity.Player

class BuildRepository(
    private val buildHttpService: BuildHttpService,
) {
    private var namesToId: MutableMap<String, Int>? = null

    private suspend fun fetchIdMap(): MutableMap<String, Int> = buildHttpService
        .names()
        .associateBy({it.name}, {it.id})
        .toMutableMap()
        .also { namesToId = it }

    suspend fun all(page: Int = 1): PaginatedResponse<List<Build>> {
        val paginated = buildHttpService.all(
            page = page,
            size = 10,
        )
        if (namesToId == null) {
            fetchIdMap()
        }
        paginated.data.forEach {
            namesToId?.put(it.name, it.id)
        }
        return paginated
    }

    suspend fun get(name: String): Build? {
        if (namesToId == null) {
            fetchIdMap()
        }
        val id = namesToId?.get(name) ?: return null
        return buildHttpService.get(id = id)
    }

    suspend fun names(): List<String> {
        if (namesToId == null) {
            fetchIdMap()
        }
        return namesToId?.map { it.key }?.toList() ?: listOf()
    }

    suspend fun create(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Build {
        val build = buildHttpService.create(
            playerUUID = player.uniqueId,
            name = name,
            world = world,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
        if (namesToId == null) {
            fetchIdMap()
        }
        namesToId?.put(build.name, build.id)
        return build
    }

    suspend fun update(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Build {
        if (namesToId == null) {
            fetchIdMap()
        }
        val id = namesToId?.get(name) ?: throw Exception("Build not found")

        return buildHttpService.update(
            id = id,
            playerUUID = player.uniqueId,
            name = name,
            world = world,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
    }

    suspend fun delete(
        name: String,
        player: Player,
    ) {
        if (namesToId == null) {
            fetchIdMap()
        }
        val id = namesToId?.get(name) ?: throw Exception("Build not found")

        buildHttpService.delete(
            id = id,
            playerUUID = player.uniqueId,
        )
    }

    suspend fun vote(name: String, player: Player): Build {
        if (namesToId == null) {
            namesToId = buildHttpService.names().associateBy({it.name}, {it.id}).toMutableMap()
        }
        val id = namesToId?.get(name) ?: throw Exception("Build not found")

        return buildHttpService.vote(
            id = id,
            playerUUID = player.uniqueId,
        )
    }
}
