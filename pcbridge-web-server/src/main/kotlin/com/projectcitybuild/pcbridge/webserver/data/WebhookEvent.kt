package com.projectcitybuild.pcbridge.webserver.data

import com.projectcitybuild.pcbridge.http.models.pcb.IPBan
import com.projectcitybuild.pcbridge.http.models.pcb.PlayerBan
import com.projectcitybuild.pcbridge.http.models.pcb.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import java.util.UUID

sealed class WebhookEvent

data class PlayerSyncRequestedWebhook(
    val playerUUID: UUID,
): WebhookEvent()

data class UUIDBanRequestedWebhook(
    val ban: PlayerBan,
): WebhookEvent()

data class IPBanRequestedWebhook(
    val ban: IPBan,
): WebhookEvent()

data class SyncWarpsWebhook(
    val warps: List<Warp>,
): WebhookEvent()

data class SyncRemoteConfigWebhook(
    val config: RemoteConfigVersion,
): WebhookEvent()
