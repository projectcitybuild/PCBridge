package com.projectcitybuild.features.teleporting.usecases

import com.projectcitybuild.core.utilities.Result
import java.util.UUID

interface TPToggleUseCase {
    enum class FailureReason {
        ALREADY_TOGGLED_ON,
        ALREADY_TOGGLED_OFF,
    }
    fun toggle(playerUUID: UUID, toggleOn: Boolean? = null): Result<Boolean, FailureReason>
}
