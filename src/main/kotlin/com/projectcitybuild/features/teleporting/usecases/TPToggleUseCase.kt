package com.projectcitybuild.features.teleporting.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import java.util.*
import javax.inject.Inject

class TPToggleUseCase @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
) {
    enum class FailureReason {
        ALREADY_TOGGLED_ON,
        ALREADY_TOGGLED_OFF,
    }

    fun toggle(playerUUID: UUID, toggleOn: Boolean?): Result<Boolean, FailureReason> {
        val playerConfig = playerConfigRepository.get(playerUUID)
            ?: throw Exception("$playerUUID player config is missing")

        // Either use the given toggle value, or reverse the current saved value
        val willToggleOn = toggleOn ?: !playerConfig.isAllowingTPs

        if (willToggleOn == playerConfig.isAllowingTPs) {
            return when (willToggleOn) {
                true -> Failure(FailureReason.ALREADY_TOGGLED_ON)
                false -> Failure(FailureReason.ALREADY_TOGGLED_OFF)
            }
        }

        playerConfig.isAllowingTPs = willToggleOn
        playerConfigRepository.save(playerConfig)

        return Success(willToggleOn)
    }
}