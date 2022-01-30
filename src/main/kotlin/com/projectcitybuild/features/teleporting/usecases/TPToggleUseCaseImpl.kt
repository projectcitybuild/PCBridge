package com.projectcitybuild.features.teleporting.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import java.util.*
import javax.inject.Inject

class TPToggleUseCaseImpl @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
): TPToggleUseCase {
    override fun toggle(playerUUID: UUID, toggleOn: Boolean?): Result<Boolean, TPToggleUseCase.FailureReason> {
        val playerConfig = playerConfigRepository.get(playerUUID)
            ?: throw Exception("$playerUUID player config is missing")

        // Either use the given toggle value, or reverse the current saved value
        val willToggleOn = toggleOn ?: !playerConfig.isAllowingTPs

        if (willToggleOn == playerConfig.isAllowingTPs) {
            return when (willToggleOn) {
                true -> Failure(TPToggleUseCase.FailureReason.ALREADY_TOGGLED_ON)
                false -> Failure(TPToggleUseCase.FailureReason.ALREADY_TOGGLED_OFF)
            }
        }

        playerConfig.isAllowingTPs = willToggleOn
        playerConfigRepository.save(playerConfig)

        return Success(willToggleOn)
    }
}