package com.projectcitybuild.features.bans.usecases.checkban

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.datetime.DateTimeFormatter
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import javax.inject.Inject

class CheckBanUseCaseImpl @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val dateTimeFormatter: DateTimeFormatter,
): CheckBanUseCase {

    override suspend fun getBan(
        targetPlayerName: String
    ): Result<CheckBanUseCase.BanRecord?, CheckBanUseCase.FailureReason> {
        val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
            ?: return Failure(CheckBanUseCase.FailureReason.PlayerDoesNotExist)

        val ban = banRepository.get(targetPlayerUUID = targetPlayerUUID)
            ?: return Success(null)

        val banRecord = CheckBanUseCase.BanRecord(
            reason = ban.reason ?: "No reason given",
            dateOfBan = ban.createdAt.let { dateTimeFormatter.convert(it) },
            expiryDate = ban.expiresAt?.let { dateTimeFormatter.convert(it) } ?: "Never"
        )
        return Success(banRecord)
    }
}