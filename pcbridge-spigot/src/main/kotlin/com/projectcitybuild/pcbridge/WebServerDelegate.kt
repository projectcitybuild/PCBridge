package com.projectcitybuild.pcbridge

import com.projectcitybuild.pcbridge.features.bans.events.IPBanRequestedEvent
import com.projectcitybuild.pcbridge.features.bans.events.UUIDBanRequestedEvent
import com.projectcitybuild.pcbridge.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.http.models.IPBan
import com.projectcitybuild.pcbridge.http.models.PlayerBan
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

    override suspend fun banPlayer(ban: PlayerBan) {
        eventBroadcaster.broadcast(
            UUIDBanRequestedEvent(ban = ban),
        )
    }

    override suspend fun banIP(ban: IPBan) {
        eventBroadcaster.broadcast(
            IPBanRequestedEvent(ban = ban),
        )
    }
}