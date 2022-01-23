package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Result

interface CheckBanUseCase {
    enum class FailureReason {
        PlayerDoesNotExist,
    }
    data class BanRecord(
        val reason: String,
        val dateOfBan: String,
        val expiryDate: String,
    )
    suspend fun getBan(targetPlayerName: String): Result<BanRecord?, FailureReason>
}

