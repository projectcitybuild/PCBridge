package com.projectcitybuild.features.bans.usecases.banip

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.modules.datetime.Time
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import javax.inject.Inject

class BanIPUseCaseImpl @Inject constructor(
    private val ipBanRepository: IPBanRepository,
    private val playerKicker: PlayerKicker,
    private val time: Time,
): BanIPUseCase {

    override fun banIP(
        ip: String,
        bannerName: String,
        reason: String?
    ): Result<Unit, BanIPUseCase.FailureReason> {
        val isValidIP = Regex.IP.matcher(ip).matches()
        if (!isValidIP) {
            return Failure(BanIPUseCase.FailureReason.INVALID_IP)
        }

        val existingBan = ipBanRepository.get(ip)
        if (existingBan != null) {
            return Failure(BanIPUseCase.FailureReason.IP_ALREADY_BANNED)
        }

        val ban = IPBan(
            ip,
            bannerName,
            reason,
            createdAt = time.now(),
        )
        ipBanRepository.put(ban)

        playerKicker.kickByIP(
            ip = ip,
            reason = "You have been banned.\nAppeal @ projectcitybuild.com",
            context = PlayerKicker.KickContext.FATAL,
        )

        return Success(Unit)
    }
}