package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.support.spigot.kick.PlayerKicker
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import java.util.UUID

class TempBanUUID(
    private val playerBanRepository: PlayerBanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val server: Server,
    private val playerKicker: PlayerKicker,
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

            playerKicker.kickByUUID(
                playerUUID = targetPlayerUUID,
                reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
                context = PlayerKicker.KickContext.FATAL,
            )

            server.broadcastMessage(
                TextComponent("$targetPlayerName has been temporarily banned by $bannerName: ${reason ?: "No reason given"}").apply {
                    color = ChatColor.GRAY
                    isItalic = true
                }.toLegacyText()
            )

            return Success(Unit)
        } catch (e: UUIDBanHttpService.UUIDAlreadyBannedException) {
            return Failure(FailureReason.PlayerAlreadyBanned)
        }
    }
}
