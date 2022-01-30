package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.WarpRepository
import java.time.LocalDateTime
import javax.inject.Inject

class CreateWarpUseCaseImpl @Inject constructor(
    private val warpRepository: WarpRepository,
): CreateWarpUseCase {

    override fun createWarp(name: String, location: CrossServerLocation): Result<Unit, CreateWarpUseCase.FailureReason> {
        if (warpRepository.exists(name)) {
            return Failure(CreateWarpUseCase.FailureReason.WARP_ALREADY_EXISTS)
        }
        val warp = Warp(
            name,
            location,
            LocalDateTime.now()
        )
        warpRepository.add(warp)

        return Success(Unit)
    }
}