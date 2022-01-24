package com.projectcitybuild.features.hub.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.LegacyWarp
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.entities.serializables.SerializableDate
import com.projectcitybuild.entities.serializables.SerializableUUID
import com.projectcitybuild.features.hub.storage.HubFileStorage
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class HubRepository @Inject constructor(
    private val hubFileStorage: HubFileStorage,
) {
    fun get(): Warp? {
        val hub = hubFileStorage.load() ?: return null

        // TODO: use something more general than a Warp
        return Warp(
            name = "hub",
            location = CrossServerLocation(
                serverName = hub.serverName,
                worldName = hub.worldName,
                x = hub.x,
                y = hub.y,
                z = hub.z,
                pitch = hub.pitch,
                yaw = hub.yaw,
            ),
            createdAt = LocalDateTime.now(),
        )
    }

    fun save(hub: Warp, playerUUID: UUID) {
        val legacyWarp = LegacyWarp(
            hub.location.serverName,
            hub.location.worldName,
            SerializableUUID(playerUUID),
            hub.location.x,
            hub.location.y,
            hub.location.z,
            hub.location.pitch,
            hub.location.yaw,
            SerializableDate(Date())
        )
        hubFileStorage.save(legacyWarp)
    }
}