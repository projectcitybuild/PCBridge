package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.entity.Player
import javax.inject.Inject

class IgnoreUseCase @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val nameGuesser: NameGuesser
) {
    enum class FailureReason {
        PLAYER_NOT_ONLINE,
        CANNOT_IGNORE_SELF,
        ALREADY_IGNORING,
    }

    fun execute(
        ignoringPlayer: Player,
        targetPlayerName: String,
        onlinePlayers: List<Player>,
    ): Result<Player, FailureReason> {
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, onlinePlayers) { it.name }
            ?: return Failure(FailureReason.PLAYER_NOT_ONLINE)

        if (ignoringPlayer == targetPlayer) {
            return Failure(FailureReason.CANNOT_IGNORE_SELF)
        }

        val playerConfig = playerConfigRepository.get(ignoringPlayer.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)

        if (chatIgnoreRepository.isIgnored(playerConfig!!.id, targetPlayerConfig!!.id)) {
            return Failure(FailureReason.ALREADY_IGNORING)
        }

        chatIgnoreRepository.add(playerConfig.id, targetPlayerConfig.id)

        return Success(targetPlayer)
    }
}