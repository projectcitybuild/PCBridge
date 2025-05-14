package com.projectcitybuild.pcbridge.paper.features.homes.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.Build
import com.projectcitybuild.pcbridge.http.pcb.models.Home
import com.projectcitybuild.pcbridge.http.pcb.models.HomeLimit
import com.projectcitybuild.pcbridge.http.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedResponse
import com.projectcitybuild.pcbridge.http.pcb.services.HomeHttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class HomeRepository(
    private val homeHttpService: HomeHttpService,
) {
    // TODO: store this elsewhere so we can evict data on player quit
    private val namesPerPlayer = mutableMapOf<UUID, List<NamedResource>>()

    suspend fun names(playerUUID: UUID): List<NamedResource> {
        val cached = namesPerPlayer[playerUUID]
        if (cached != null) {
            return cached
        }
        val names = homeHttpService.names(playerUUID)
        namesPerPlayer[playerUUID] = names
        return names
    }

    suspend fun all(
        playerUUID: UUID,
        page: Int = 1,
    ): PaginatedResponse<List<Home>> {
        return homeHttpService.all(
            playerUUID = playerUUID,
            page = page,
            size = 10,
        )
    }

    suspend fun get(
        playerUUID: UUID,
        name: String,
    ): Home? = withContext(Dispatchers.IO) {
        val nameList = names(playerUUID)
        val match = nameList.firstOrNull { it.name == name }
        checkNotNull(match) { "Home ($name) not found" }

        homeHttpService.get(playerUUID, match.id)
    }

    suspend fun create(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Home {
        val home = homeHttpService.create(
            playerUUID = player.uniqueId,
            name = name,
            world = world,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
        namesPerPlayer[player.uniqueId] = names(player.uniqueId)
            .toMutableList()
            .also {
                val namedResource = NamedResource.fromHome(home)
                // In case names() fetched from remote and already has the fresh data
                if (!it.contains(namedResource)) {
                    it.add(NamedResource.fromHome(home))
                }
            }

        return home
    }

    suspend fun move(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Home {
        val nameList = names(player.uniqueId)
        val home = nameList.firstOrNull { it.name == name }
        checkNotNull(home) { "Home ($name) not found" }

        val updatedHome = homeHttpService.update(
            id = home.id,
            playerUUID = player.uniqueId,
            name = name,
            world = world,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
        return updatedHome
    }

    suspend fun rename(
        id: Int,
        newName: String,
        player: Player,
    ): Home {
        val home = homeHttpService.get(player.uniqueId, id)
        checkNotNull(home) { "Home not found" }

        val updatedHome = homeHttpService.update(
            id = id,
            playerUUID = player.uniqueId,
            name = newName,
            world = home.world,
            x = home.x,
            y = home.y,
            z = home.z,
            pitch = home.pitch,
            yaw = home.yaw,
        )
        namesPerPlayer[player.uniqueId] = names(player.uniqueId).map {
            if (it.id == updatedHome.id) NamedResource.fromHome(updatedHome)
            else it
        }
        return updatedHome
    }

    suspend fun delete(
        player: Player,
        name: String,
    ) {
        val nameList = names(player.uniqueId)
        val match = nameList.firstOrNull { it.name == name }
        checkNotNull(match) { "Home ($name) not found" }

        homeHttpService.delete(
            playerUUID = player.uniqueId,
            id = match.id,
        )
        namesPerPlayer[player.uniqueId] = nameList
            .filter { it.id != match.id }
    }

    suspend fun limit(player: Player): HomeLimit {
        return homeHttpService.limit(player.uniqueId)
    }
}

private fun NamedResource.Companion.fromHome(home: Home): NamedResource
    = NamedResource(id = home.id, name = home.name)