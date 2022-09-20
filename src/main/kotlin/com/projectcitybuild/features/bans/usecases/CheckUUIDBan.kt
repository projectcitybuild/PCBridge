package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import javax.inject.Inject

class CheckUUIDBan @Inject constructor(
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
