package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.repositories.IPBanRepository
import java.util.UUID
import javax.inject.Inject

class UnbanIPUseCase @Inject constructor(
    private val ipBanRepository: IPBanRepository,
) {
    enum class FailureReason {
        IP_NOT_BANNED,
        INVALID_IP,
    }

    suspend fun unbanIP(
        ip: String,
        unbannerUUID: UUID,
        unbannerName: String,
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer().sanitizedIP(ip)

        val isValidIP = Regex.IP.matcher(sanitizedIP).matches()
        if (!isValidIP) {
            return Failure(FailureReason.INVALID_IP)
        }

        try {
            ipBanRepository.unban(
                ip = ip,
                unbannerUUID = unbannerUUID,
                unbannerName = unbannerName,
            )
        } catch (e: IPBanRepository.IPNotBannedException) {
            return Failure(FailureReason.IP_NOT_BANNED)
        }

        return Success(Unit)
    }
}
