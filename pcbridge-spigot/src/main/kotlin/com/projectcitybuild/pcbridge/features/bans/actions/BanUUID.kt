package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerUUIDRepository
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Result
import com.projectcitybuild.pcbridge.utils.Success
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.event.player.PlayerKickEvent
import java.util.UUID

class BanUUID(
    private val playerBanRepository: PlayerBanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val server: Server,
) {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerAlreadyBanned,
    }

    suspend fun ban(
        targetPlayerName: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String?,
    ): Result<Unit, FailureReason> {
        try {
            val targetPlayerUUID =
                playerUUIDRepository.get(targetPlayerName)
                    ?: return Failure(FailureReason.PlayerDoesNotExist)

            playerBanRepository.ban(
                targetPlayerUUID = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                bannerPlayerUUID = bannerUUID,
                bannerPlayerName = bannerName,
                reason = reason,
            )

            server.getPlayer(targetPlayerUUID)?.kick(
                Component.text("You have been banned.\n\nAppeal @ projectcitybuild.com"),
                PlayerKickEvent.Cause.BANNED,
            )

            return Success(Unit)
        } catch (e: UUIDBanHttpService.UUIDAlreadyBannedException) {
            return Failure(FailureReason.PlayerAlreadyBanned)
        }
    }
}
