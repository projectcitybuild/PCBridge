package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import java.util.UUID
import javax.inject.Inject

class UnbanUseCase @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val server: Server,
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

            server.broadcastMessage(
                TextComponent("$targetPlayerName has been unbanned").apply {
                    color = ChatColor.GRAY
                    isItalic = true
                }.toLegacyText()
            )

            return Success(Unit)

        } catch (e: BanRepository.PlayerNotBannedException) {
            return Failure(FailureReason.PlayerNotBanned)
        }
    }
}
