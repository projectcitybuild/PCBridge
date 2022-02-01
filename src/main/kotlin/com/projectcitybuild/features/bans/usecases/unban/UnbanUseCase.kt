package com.projectcitybuild.features.bans.usecases.unban

import com.projectcitybuild.core.utilities.Result
import java.util.*

interface UnbanUseCase {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerNotBanned,
    }
    suspend fun unban(
        targetPlayerName: String,
        bannerUUID: UUID?,
    ): Result<Unit, FailureReason>
}

