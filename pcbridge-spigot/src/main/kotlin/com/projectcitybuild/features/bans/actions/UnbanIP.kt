package com.projectcitybuild.features.bans.actions

import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.core.utils.helpers.isValidIP
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import java.util.UUID

class UnbanIP(
    private val ipBanRepository: IPBanRepository,
) {
    enum class FailureReason {
        IP_NOT_BANNED,
        INVALID_IP,
    }

    suspend fun execute(
        ip: String,
        unbannerUUID: UUID?,
        unbannerName: String,
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer.sanitizedIP(ip)
        if (!isValidIP(sanitizedIP)) {
            return Failure(FailureReason.INVALID_IP)
        }

        try {
            ipBanRepository.unban(
                ip = ip,
                unbannerUUID = unbannerUUID,
                unbannerName = unbannerName,
            )
        } catch (e: IPBanHttpService.IPNotBannedException) {
            return Failure(FailureReason.IP_NOT_BANNED)
        }

        return Success(Unit)
    }
}
