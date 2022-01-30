package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.repositories.WarpRepository
import javax.inject.Inject

class DeleteWarpUseCaseImpl @Inject constructor(
    private val warpRepository: WarpRepository,
): DeleteWarpUseCase {

    override fun deleteWarp(name: String): Result<Unit, DeleteWarpUseCase.FailureReason> {
        if (!warpRepository.exists(name)) {
            return Failure(DeleteWarpUseCase.FailureReason.WARP_NOT_FOUND)
        }
        // TODO: Add confirmation
        warpRepository.delete(name)

        return Success(Unit)
    }
}