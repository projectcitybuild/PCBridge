package com.projectcitybuild.modules.moderation.bans.actions

import com.projectcitybuild.utilities.helpers.Regex
import com.projectcitybuild.utilities.helpers.Sanitizer
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.support.spigot.SpigotServer
import java.util.UUID

class BanIP(
    private val ipBanRepository: IPBanRepository,
    private val server: SpigotServer,
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
        } catch (e: IPBanHttpService.IPAlreadyBannedException) {
            return Failure(FailureReason.IP_ALREADY_BANNED)
        }

        server.kickByIP(
            ip = ip,
            reason = "You have been banned.\n\nAppeal @ projectcitybuild.com",
            context = SpigotServer.KickContext.FATAL,
        )

        return Success(Unit)
    }
}
