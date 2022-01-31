package com.projectcitybuild.features.warps.usecases.createwarp

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.datetime.Time
import javax.inject.Inject

class CreateWarpUseCaseImpl @Inject constructor(
    private val warpRepository: WarpRepository,
    private val time: Time,
): CreateWarpUseCase {

    override fun createWarp(name: String, location: CrossServerLocation): Result<Unit, CreateWarpUseCase.FailureReason> {
        if (warpRepository.exists(name)) {
            return Failure(CreateWarpUseCase.FailureReason.WARP_ALREADY_EXISTS)
        }
        val warp = Warp(
            name,
            location,
            time.now()
        )
        warpRepository.add(warp)

        return Success(Unit)
    }
}