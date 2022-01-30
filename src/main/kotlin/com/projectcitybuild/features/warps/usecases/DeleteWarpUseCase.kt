package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Result

interface DeleteWarpUseCase {
    enum class FailureReason {
        WARP_NOT_FOUND,
    }
    fun deleteWarp(name: String): Result<Unit, FailureReason>
}
