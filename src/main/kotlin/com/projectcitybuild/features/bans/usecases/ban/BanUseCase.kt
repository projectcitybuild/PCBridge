package com.projectcitybuild.features.bans.usecases.ban

import com.projectcitybuild.core.utilities.Result
import java.util.*

interface BanUseCase {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerAlreadyBanned,
    }
    suspend fun ban(
        targetPlayerName: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String?
    ): Result<Unit, FailureReason>
}

