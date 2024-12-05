package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.paper.features.bans.events.IPBanRequestedEvent
import com.projectcitybuild.pcbridge.paper.features.bans.events.UUIDBanRequestedEvent
import com.projectcitybuild.pcbridge.paper.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.models.pcb.IPBan
import com.projectcitybuild.pcbridge.http.models.pcb.PlayerBan
import com.projectcitybuild.pcbridge.http.models.pcb.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.webserver.HttpServerDelegate
import java.util.UUID

class WebServerDelegate(
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val remoteConfig: RemoteConfig,
    private val warpRepository: WarpRepository,
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

    override suspend fun updateConfig(config: RemoteConfigVersion) {
        log.debug { "Updating config to version ${config.version}" }
        remoteConfig.set(config)
    }

    override suspend fun syncWarps(warps: List<Warp>) {
        log.debug { "Invalidating warp cache" }
        warpRepository.reload()
    }
}