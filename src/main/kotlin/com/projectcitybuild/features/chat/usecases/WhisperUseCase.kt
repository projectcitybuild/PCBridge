package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.chat.Whisperer
import org.bukkit.entity.Player
import javax.inject.Inject

class WhisperUseCase @Inject constructor(
    private val whisperer: Whisperer,
) {
    enum class FailureReason {
        PLAYER_NOT_ONLINE,
        CANNOT_WHISPER_SELF,
        BEING_IGNORED,
    }

    fun execute(
        whisperingPlayer: Player,
        targetPlayerName: String,
        onlinePlayers: List<Player>,
        message: String,
    ): Result<Unit, FailureReason> {
        try {
            whisperer.execute(
                whisperingPlayer = whisperingPlayer,
                targetPlayerName = targetPlayerName,
                onlinePlayers = onlinePlayers,
                message = message,
            )
        } catch (error: Whisperer.PlayerNotOnlineException) {
            return Failure(FailureReason.PLAYER_NOT_ONLINE)
        } catch (error: Whisperer.CannotWhisperSelfException) {
            return Failure(FailureReason.CANNOT_WHISPER_SELF)
        } catch (error: Whisperer.BeingIgnoredException) {
            return Failure(FailureReason.BEING_IGNORED)
        }
        return Success(Unit)
    }
}
