package com.projectcitybuild.features.bans.actions

import com.projectcitybuild.core.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.utils.Failure
import com.projectcitybuild.utils.Result
import com.projectcitybuild.utils.Success
import com.projectcitybuild.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.features.bans.repositories.PlayerUUIDRepository

class CheckUUIDBan(
    private val playerBanRepository: PlayerBanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val dateTimeFormatter: DateTimeFormatter,
) {
    enum class FailureReason {
        PLAYER_DOES_NOT_EXIST,
    }

    data class BanRecord(
        val reason: String,
        val dateOfBan: String,
        val expiryDate: String,
    )

    suspend fun getBan(
        targetPlayerName: String
    ): Result<BanRecord?, FailureReason> {
        val targetPlayerUUID = playerUUIDRepository.get(targetPlayerName)
            ?: return Failure(FailureReason.PLAYER_DOES_NOT_EXIST)

        val ban = playerBanRepository.get(targetPlayerUUID = targetPlayerUUID)
            ?: return Success(null)

        val banRecord = BanRecord(
            reason = ban.reason ?: "No reason given",
            dateOfBan = ban.createdAt.let { dateTimeFormatter.convert(it) },
            expiryDate = ban.expiresAt?.let { dateTimeFormatter.convert(it) } ?: "Never"
        )
        return Success(banRecord)
    }
}
