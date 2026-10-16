package com.projectcitybuild.pcbridge.platform.webserver.data

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.IPBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import java.util.UUID

sealed class WebhookEvent {
    data class PlayerSyncRequested(
        val playerUUID: UUID,
    ): WebhookEvent()

    data class UUIDBanRequested(
        val ban: PlayerBan,
    ): WebhookEvent()

    data class IPBanRequested(
        val ban: IPBan,
    ): WebhookEvent()

    data class SyncWarps(
        val warpNames: List<NamedResource>,
    ): WebhookEvent()

    data class SyncRemoteConfig(
        val config: RemoteConfigVersion,
    ): WebhookEvent()
}
