package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import com.projectcitybuild.modules.proxyadapter.messages.TextComponentBox
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import javax.inject.Inject

class BanUseCase @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerKicker: PlayerKicker,
    private val messageBroadcaster: MessageBroadcaster,
) {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerAlreadyBanned,
    }

    suspend fun ban(
        targetPlayerName: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String?
    ): Result<Unit, FailureReason> {
        try {
            val targetPlayerUUID = playerUUIDRepository.get(targetPlayerName)
                ?: return Failure(FailureReason.PlayerDoesNotExist)

            banRepository.ban(
                targetPlayerUUID = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                staffId = bannerUUID,
                reason = reason
            )
            messageBroadcaster.broadcastToAll(
                TextComponentBox(
                    TextComponent("$targetPlayerName has been banned by ${bannerName}: ${reason ?: "No reason given"}").apply {
                        color = ChatColor.GRAY
                        isItalic = true
                    }
                )
            )
            playerKicker.kickByUUID(
                playerUUID = targetPlayerUUID,
                reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
                context = PlayerKicker.KickContext.FATAL,
            )
            return Success(Unit)
        }
        catch (e: BanRepository.PlayerAlreadyBannedException) {
            return Failure(FailureReason.PlayerAlreadyBanned)
        }
    }
}