package com.projectcitybuild.pcbridge.paper.features.builds.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.Build
import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedResponse
import com.projectcitybuild.pcbridge.http.pcb.services.BuildHttpService
import com.projectcitybuild.pcbridge.paper.core.utils.Trie
import com.projectcitybuild.pcbridge.paper.features.builds.data.EditableBuildField
import com.projectcitybuild.pcbridge.paper.features.homes.data.EditableHomeField
import org.bukkit.Location
import org.bukkit.entity.Player

class BuildRepository(
    private val buildHttpService: BuildHttpService,
) {
    class IdMap(
        initial: Map<String, Int>,
    ) {
        private val map: MutableMap<String, Int> = initial.toMutableMap()
        private val names = Trie().apply {
            initial.keys.forEach { insert(it) }
        }

        fun put(build: Build) {
            map[build.name] = build.id
            names.insert(build.name)
        }

        fun get(name: String): Int? {
            return map[name]
        }

        fun getNames(prefix: String): List<String> {
            return names.matchingPrefix(prefix = prefix)
        }

        fun remove(name: String) {
            map.remove(name)
            names.remove(name)
        }

        fun update(id: Int, newName: String) {
            val currentNameForId = map.filter { entry -> entry.value == id }.map { it.key }.firstOrNull()
            if (currentNameForId != null) {
                map.remove(currentNameForId)
                map[newName] = id
                names.remove(currentNameForId)
                names.insert(newName)
            }
        }
    }

    private var cache: IdMap? = null

    private suspend fun fetchIdMap(): Map<String, Int> = buildHttpService
        .names()
        .associateBy({it.name}, {it.id})
        .also { cache = IdMap(it) }

    suspend fun all(page: Int = 1): PaginatedResponse<List<Build>> {
        return buildHttpService.all(
            page = page,
            size = 10,
        )
    }

    suspend fun get(name: String): Build? {
        if (cache == null) {
            fetchIdMap()
        }
        val id = cache?.get(name) ?: return null
        return buildHttpService.get(id = id)
    }

    suspend fun names(prefix: String = ""): List<String> {
        if (cache == null) {
            fetchIdMap()
        }
        return cache?.getNames(prefix) ?: emptyList()
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
        cache?.put(build)
        return build
    }

    suspend fun update(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Build {
        if (cache == null) {
            fetchIdMap()
        }
        val id = cache?.get(name) ?: throw IllegalStateException("Build not found")

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
        if (cache == null) {
            fetchIdMap()
        }
        val id = cache?.get(name) ?: throw IllegalStateException("Build not found")

        buildHttpService.delete(
            id = id,
            playerUUID = player.uniqueId,
        )
        cache?.remove(name)
    }

    suspend fun vote(name: String, player: Player): Build {
        if (cache == null) {
            fetchIdMap()
        }
        val id = cache?.get(name) ?: throw IllegalStateException("Build not found")

        return buildHttpService.vote(
            id = id,
            playerUUID = player.uniqueId,
        )
    }

    suspend fun unvote(name: String, player: Player): Build {
        if (cache == null) {
            fetchIdMap()
        }
        val id = cache?.get(name) ?: throw IllegalStateException("Build not found")

        return buildHttpService.unvote(
            id = id,
            playerUUID = player.uniqueId,
        )
    }

    suspend fun set(
        id: Int,
        player: Player,
        field: EditableBuildField,
        value: String,
    ): Build {
        val build = buildHttpService.set(
            id = id,
            playerUUID = player.uniqueId,
            name = if (field == EditableBuildField.NAME) value else null,
            description = if (field == EditableBuildField.DESCRIPTION) value else null,
            lore = if (field == EditableBuildField.LORE) value else null,
        )
        if (field == EditableBuildField.NAME) {
            cache?.update(id, newName = build.name)
        }
        return build
    }
}
