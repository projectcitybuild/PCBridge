package com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories

import com.projectcitybuild.pcbridge.http.models.pcb.Build
import com.projectcitybuild.pcbridge.http.models.pcb.BuildName
import com.projectcitybuild.pcbridge.http.models.pcb.PaginatedResponse
import com.projectcitybuild.pcbridge.http.services.pcb.BuildHttpService

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
        return null
    }

    suspend fun names(): List<String> {
        if (names != null) {
            return names!!.map { it.name }
        }
        return buildHttpService.names()
            .also { names = it }
            .map { it.name }
    }
}
