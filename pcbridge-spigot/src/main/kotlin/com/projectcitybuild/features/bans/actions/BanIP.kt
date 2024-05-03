package com.projectcitybuild.features.bans.actions

import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.utils.Sanitizer
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.core.utils.helpers.isValidIP
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
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
        bannerUUID: UUID?,
        bannerName: String,
        reason: String,
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer.sanitizedIP(ip)
        if (!isValidIP(sanitizedIP)) {
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
