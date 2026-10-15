package com.projectcitybuild.pcbridge.webserver.data

import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
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
