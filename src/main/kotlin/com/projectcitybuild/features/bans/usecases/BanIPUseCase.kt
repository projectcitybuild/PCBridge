package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import javax.inject.Inject

class BanIPUseCase @Inject constructor(
    private val ipBanRepository: IPBanRepository,
    private val playerKicker: PlayerKicker,
    private val time: Time,
) {
    enum class FailureReason {
        IP_ALREADY_BANNED,
        INVALID_IP,
    }

    fun banIP(
        ip: String,
        bannerName: String?,
        reason: String?
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer().sanitizedIP(ip)

        val isValidIP = Regex.IP.matcher(sanitizedIP).matches()
        if (!isValidIP) {
            return Failure(FailureReason.INVALID_IP)
        }

        val existingBan = ipBanRepository.get(sanitizedIP)
        if (existingBan != null) {
            return Failure(FailureReason.IP_ALREADY_BANNED)
        }

        val ban = IPBan(
            ip = sanitizedIP,
            bannerName = bannerName,
            reason = reason ?: "",
            createdAt = time.now(),
        )
        ipBanRepository.put(ban)

        playerKicker.kickByIP(
            ip = ip,
            reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
            context = PlayerKicker.KickContext.FATAL,
        )

        return Success(Unit)
    }
}