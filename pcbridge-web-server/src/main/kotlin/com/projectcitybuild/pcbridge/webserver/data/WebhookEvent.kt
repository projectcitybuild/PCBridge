package com.projectcitybuild.pcbridge.webserver.data

import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
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
