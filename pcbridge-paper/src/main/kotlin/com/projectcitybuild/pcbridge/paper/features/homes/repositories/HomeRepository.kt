package com.projectcitybuild.pcbridge.paper.features.homes.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.Home
import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedResponse
import com.projectcitybuild.pcbridge.http.pcb.services.HomeHttpService
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class HomeRepository(
    private val homeHttpService: HomeHttpService,
) {
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

    suspend fun create(
        name: String,
        world: String,
        location: Location,
        player: Player,
    ): Home {
        return homeHttpService.create(
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
        player: Player,
        id: Int,
    ) {
        homeHttpService.delete(
            playerUUID = player.uniqueId,
            id = id,
        )
    }
}
