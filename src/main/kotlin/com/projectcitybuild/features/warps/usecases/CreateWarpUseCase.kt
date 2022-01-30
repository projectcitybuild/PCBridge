package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.entities.CrossServerLocation

interface CreateWarpUseCase {
    enum class FailureReason {
        WARP_ALREADY_EXISTS,
    }
    fun createWarp(name: String, location: CrossServerLocation): Result<Unit, FailureReason>
}
