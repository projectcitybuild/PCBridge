package com.projectcitybuild.features.bans.actions

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.support.spigot.SpigotServer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.util.UUID

class TempBanUUID(
    private val playerBanRepository: PlayerBanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val server: SpigotServer,
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
        expiryDate: Long,
    ): Result<Unit, FailureReason> {
        try {
            val targetPlayerUUID = playerUUIDRepository.get(targetPlayerName)
                ?: return Failure(FailureReason.PlayerDoesNotExist)

            playerBanRepository.ban(
                targetPlayerUUID = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                bannerPlayerUUID = bannerUUID,
                bannerPlayerName = bannerName,
                reason = reason,
                expiryDate = expiryDate,
            )

            server.kickByUUID(
                playerUUID = targetPlayerUUID,
                reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
                context = SpigotServer.KickContext.FATAL,
            )

            // TODO: move this to command
            server.broadcastMessage(
                TextComponent("$targetPlayerName has been temporarily banned by $bannerName: ${reason ?: "No reason given"}").apply {
                    color = ChatColor.GRAY
                    isItalic = true
                }
            )

            return Success(Unit)
        } catch (e: UUIDBanHttpService.UUIDAlreadyBannedException) {
            return Failure(FailureReason.PlayerAlreadyBanned)
        }
    }
}
