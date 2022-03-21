package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.messages.TextComponentBox
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import javax.inject.Inject

class UnbanUseCase @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val messageBroadcaster: MessageBroadcaster,
) {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerNotBanned,
    }

    suspend fun unban(
        targetPlayerName: String,
        bannerUUID: UUID?,
    ): Result<Unit, FailureReason> {
        try {
            val targetPlayerUUID = playerUUIDRepository.get(targetPlayerName)
                ?: return Failure(FailureReason.PlayerDoesNotExist)

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
        } catch (e: BanRepository.PlayerNotBannedException) {
            return Failure(FailureReason.PlayerNotBanned)
        }
    }
}
