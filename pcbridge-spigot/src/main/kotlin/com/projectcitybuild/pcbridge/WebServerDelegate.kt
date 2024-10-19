package com.projectcitybuild.pcbridge

import com.projectcitybuild.pcbridge.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.webserver.HttpServerDelegate
import java.util.UUID

class WebServerDelegate(
    private val eventBroadcaster: SpigotEventBroadcaster,
) : HttpServerDelegate {
    override suspend fun syncPlayer(uuid: UUID) {
        eventBroadcaster.broadcast(
            PlayerSyncRequestedEvent(playerUUID = uuid),
        )
    }
}