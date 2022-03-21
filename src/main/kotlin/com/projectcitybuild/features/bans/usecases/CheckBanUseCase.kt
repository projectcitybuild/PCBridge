package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import javax.inject.Inject

class CheckBanUseCase @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val dateTimeFormatter: DateTimeFormatter,
) {
    enum class FailureReason {
        PlayerDoesNotExist,
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
            ?: return Failure(FailureReason.PlayerDoesNotExist)

        val ban = banRepository.get(targetPlayerUUID = targetPlayerUUID)
            ?: return Success(null)

        val banRecord = BanRecord(
            reason = ban.reason ?: "No reason given",
            dateOfBan = ban.createdAt.let { dateTimeFormatter.convert(it) },
            expiryDate = ban.expiresAt?.let { dateTimeFormatter.convert(it) } ?: "Never"
        )
        return Success(banRecord)
    }
}
