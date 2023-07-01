package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.entity.Player

class MutePlayer(
    private val playerConfigRepository: PlayerConfigRepository,
    private val nameGuesser: NameGuesser,
) {
    enum class FailureReason {
        PLAYER_NOT_ONLINE,
    }

    fun execute(
        willBeMuted: Boolean,
        targetPlayerName: String,
        onlinePlayers: List<Player>,
    ): Result<Player, FailureReason> {
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, onlinePlayers) { it.name }
            ?: return Failure(FailureReason.PLAYER_NOT_ONLINE)

        val targetPlayerConfig = playerConfigRepository
            .get(targetPlayer.uniqueId)!!
            .also { it.isMuted = willBeMuted }

        playerConfigRepository.save(targetPlayerConfig)

        return Success(targetPlayer)
    }
}
