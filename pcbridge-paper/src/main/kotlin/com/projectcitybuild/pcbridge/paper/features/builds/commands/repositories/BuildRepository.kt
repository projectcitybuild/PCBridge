package com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories

import com.projectcitybuild.pcbridge.http.models.pcb.Build
import com.projectcitybuild.pcbridge.http.models.pcb.BuildName
import com.projectcitybuild.pcbridge.http.models.pcb.PaginatedResponse
import com.projectcitybuild.pcbridge.http.services.pcb.BuildHttpService
import org.bukkit.Location
import org.bukkit.entity.Player

class BuildRepository(
    private val buildHttpService: BuildHttpService,
) {
    private var names: List<BuildName>? = null
    private var builds: MutableMap<String, Build>? = null

    suspend fun all(page: Int = 1): PaginatedResponse<List<Build>> {
        val paginated = buildHttpService.all(
            page = page,
            size = 10,
        )
        paginated.data.forEach {
            if (builds == null) builds = mutableMapOf()
            builds?.put(it.name, it)
        }
        return paginated
    }

    suspend fun get(name: String): Build? {
        return buildHttpService.getByName(name)
    }

    suspend fun names(): List<String> {
        if (names != null) {
            return names!!.map { it.name }
        }
        return buildHttpService.names()
            .also { names = it }
            .map { it.name }
    }

    suspend fun create(name: String, world: String, location: Location, player: Player): Build {
        return buildHttpService.create(
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
}
