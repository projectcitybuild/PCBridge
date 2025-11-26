package com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
import com.projectcitybuild.pcbridge.http.pcb.services.WarpHttpService
import org.bukkit.Location

class WarpRepository(
    private val warpHttpService: WarpHttpService,
) {
    private var cachedNames: MutableList<NamedResource>? = null

    suspend fun names(): List<NamedResource> {
        if (cachedNames != null) {
            return cachedNames!!
        }
        val names = warpHttpService.names()
        cachedNames = names.toMutableList()
        return names
    }

    suspend fun all(page: Int = 1, size: Int): PaginatedList<Warp> {
        return warpHttpService.all(
            page = page,
            size = size,
        )
    }

    suspend fun all(): List<Warp> = warpHttpService.all()

    suspend fun get(name: String): Warp? {
        val nameList = names()
        val match = nameList.firstOrNull { it.name == name }
        checkNotNull(match) { "Warp ($name) not found" }

        return warpHttpService.get(match.id)
    }

    suspend fun create(
        name: String,
        location: Location,
    ): Warp {
        val warp = warpHttpService.create(
            name = name,
            world = location.world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
        cachedNames = names().toMutableList().also {
            val namedResource = NamedResource.fromWarp(warp)
            if (!it.contains(namedResource)) {
                it.add(namedResource)
            }
        }
        return warp
    }

    suspend fun move(
        name: String,
        location: Location,
    ): Warp {
        val warp = names().firstOrNull { it.name == name }
        checkNotNull(warp) { "Warp ($name) not found" }

        return warpHttpService.update(
            id = warp.id,
            name = name,
            world = location.world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
    }

    suspend fun rename(
        id: Int,
        newName: String,
    ): Warp {
        val warp = warpHttpService.get(id)
        checkNotNull(warp) { "Warp not found" }

        val updatedWarp = warpHttpService.update(
            id = id,
            name = newName,
            world = warp.world,
            x = warp.x,
            y = warp.y,
            z = warp.z,
            pitch = warp.pitch,
            yaw = warp.yaw,
        )
        cachedNames = names().map {
            if (it.id == updatedWarp.id) NamedResource.fromWarp(updatedWarp)
            else it
        }.toMutableList()

        return updatedWarp
    }

    suspend fun delete(name: String) {
        val nameList = names()
        val match = nameList.firstOrNull { it.name == name }
        checkNotNull(match) { "Warp ($name) not found" }

        warpHttpService.delete(id = match.id)
        cachedNames = nameList
            .filter { it.id != match.id }
            .toMutableList()
    }

    fun setNames(names: List<NamedResource>) {
        cachedNames = names.toMutableList()
    }
}

private fun NamedResource.Companion.fromWarp(warp: Warp): NamedResource
    = NamedResource(id = warp.id, name = warp.name)