package com.projectcitybuild.features.bans.usecases.unban

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.messages.TextComponentBox
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.util.UUID
import javax.inject.Inject

class UnbanUseCaseImpl @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val messageBroadcaster: MessageBroadcaster,
): UnbanUseCase {

    override suspend fun unban(
        targetPlayerName: String,
        bannerUUID: UUID?,
    ): Result<Unit, UnbanUseCase.FailureReason> {
        try {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
                ?: return Failure(UnbanUseCase.FailureReason.PlayerDoesNotExist)

            banRepository.unban(
                targetPlayerUUID = targetPlayerUUID,
                staffId = bannerUUID,
            )
            messageBroadcaster.broadcastToAll(
                TextComponentBox(
                    TextComponent("$targetPlayerName has been unbanned").apply {
                        color = ChatColor.GRAY
                        isItalic = true
                    }
                )
            )
            return Success(Unit)
        }
        catch (e: BanRepository.PlayerNotBannedException) {
            return Failure(UnbanUseCase.FailureReason.PlayerNotBanned)
        }
    }
}