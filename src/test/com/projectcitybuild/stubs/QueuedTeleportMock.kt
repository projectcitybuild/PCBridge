package com.projectcitybuild.stubs

import com.projectcitybuild.entities.QueuedTeleport
import com.projectcitybuild.entities.TeleportType
import java.time.LocalDateTime
import java.util.UUID

fun QueuedTeleportMock(
    playerUUID: UUID = UUID.randomUUID(),
    targetPlayerUUID: UUID = UUID.randomUUID(),
    targetServerName: String = "target_server",
    teleportType: TeleportType = TeleportType.TP,
    isSilentTeleport: Boolean = false,
): QueuedTeleport {
    return QueuedTeleport(
        playerUUID = playerUUID,
        targetPlayerUUID = targetPlayerUUID,
        targetServerName = targetServerName,
        teleportType = teleportType,
        isSilentTeleport = isSilentTeleport,
        createdAt = LocalDateTime.now(),
    )
}
