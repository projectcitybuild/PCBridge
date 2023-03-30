package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.support.spigot.kick.PlayerKicker
import java.util.UUID

class BanIP(
    private val ipBanRepository: IPBanRepository,
    private val playerKicker: PlayerKicker,
) {
    enum class FailureReason {
        IP_ALREADY_BANNED,
        INVALID_IP,
    }

    suspend fun execute(
        ip: String,
        bannerUUID: UUID,
        bannerName: String,
        reason: String,
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer().sanitizedIP(ip)

        val isValidIP = Regex.IP.matcher(sanitizedIP).matches()
        if (!isValidIP) {
            return Failure(FailureReason.INVALID_IP)
        }

        try {
            ipBanRepository.ban(
                ip = sanitizedIP,
                bannerUUID = bannerUUID,
                bannerName = bannerName,
                reason = reason,
            )
        } catch (e: IPBanRepository.IPAlreadyBannedException) {
            return Failure(FailureReason.IP_ALREADY_BANNED)
        }

        playerKicker.kickByIP(
            ip = ip,
            reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
            context = PlayerKicker.KickContext.FATAL,
        )

        return Success(Unit)
    }
}
