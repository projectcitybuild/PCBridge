package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.MessageBroadcaster
import com.projectcitybuild.features.bans.PlayerKicker
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import javax.inject.Inject

class BanUseCaseImpl @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerKicker: PlayerKicker,
    private val messageBroadcaster: MessageBroadcaster,
): BanUseCase {

    override suspend fun ban(
        targetPlayerName: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String?
    ): Result<Unit, BanUseCase.FailureReason> {
        try {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
                ?: return Failure(BanUseCase.FailureReason.PlayerDoesNotExist)

            banRepository.ban(
                targetPlayerUUID = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                staffId = bannerUUID,
                reason = reason
            )
            messageBroadcaster.broadcastToAll(
                TextComponent("${ChatColor.GRAY}$targetPlayerName has been banned by ${bannerName}: ${reason ?: "No reason given"}")
            )
            playerKicker.kick(
                playerUUID = targetPlayerUUID,
                reason = TextComponent("You have been banned.\nAppeal @ projectcitybuild.com").apply {
                    color = ChatColor.RED
                }
            )
            return Success(Unit)
        }
        catch (e: BanRepository.PlayerAlreadyBannedException) {
            return Failure(BanUseCase.FailureReason.PlayerAlreadyBanned)
        }
    }
}