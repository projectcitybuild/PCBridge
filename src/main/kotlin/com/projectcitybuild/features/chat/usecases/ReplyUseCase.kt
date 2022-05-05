package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.chat.Whisperer
import com.projectcitybuild.repositories.LastWhisperedRepository
import org.bukkit.entity.Player
import javax.inject.Inject

class ReplyUseCase @Inject constructor(
    private val whisperer: Whisperer,
    private val lastWhisperedRepository: LastWhisperedRepository,
) {
    sealed class FailureReason {
        object NO_ONE_TO_REPLY_TO : FailureReason()
        object PLAYER_NOT_ONLINE : FailureReason()
        data class IGNORED(val targetPlayerName: String) : FailureReason()
    }

    fun execute(
        whisperingPlayer: Player,
        onlinePlayers: List<Player>,
        message: String,
    ): Result<Unit, FailureReason> {
        val playerUUIDWhoLastWhispered = lastWhisperedRepository.getLastWhisperer(whisperingPlayer.uniqueId)
            ?: return Failure(FailureReason.NO_ONE_TO_REPLY_TO)

        try {
            whisperer.execute(
                whisperingPlayer = whisperingPlayer,
                targetPlayerUUID = playerUUIDWhoLastWhispered,
                onlinePlayers = onlinePlayers,
                message = message,
            )
        } catch (error: Whisperer.PlayerNotOnlineException) {
            return Failure(FailureReason.PLAYER_NOT_ONLINE)
        } catch (error: Whisperer.BeingIgnoredException) {
            return Failure(FailureReason.IGNORED(targetPlayerName = error.targetPlayerName))
        }
        return Success(Unit)
    }
}
