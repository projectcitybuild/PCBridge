package com.projectcitybuild.modules.warps.actions

import com.projectcitybuild.entities.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster

class DeleteWarp(
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
