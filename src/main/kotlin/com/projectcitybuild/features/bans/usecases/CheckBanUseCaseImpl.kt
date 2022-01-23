package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CheckBanUseCaseImpl @Inject constructor(
    private val banRepository: BanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
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
            dateOfBan = ban.createdAt.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            },
            expiryDate = ban.expiresAt?.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            } ?: "Never"
        )
        return Success(banRecord)
    }
}