package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.repositories.WarpRepository
import javax.inject.Inject

class DeleteWarpUseCase @Inject constructor(
    private val warpRepository: WarpRepository,
    private val localEventBroadcaster: LocalEventBroadcaster,
) {
    enum class FailureReason {
        WARP_NOT_FOUND,
    }

    fun deleteWarp(name: String): Result<Unit, FailureReason> {
        if (!warpRepository.exists(name)) {
            return Failure(FailureReason.WARP_NOT_FOUND)
        }
        // TODO: Add confirmation
        warpRepository.delete(name)

        localEventBroadcaster.emit(WarpDeleteEvent())

        return Success(Unit)
    }
}
