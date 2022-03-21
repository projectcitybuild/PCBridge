package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.repositories.IPBanRepository
import javax.inject.Inject

class UnbanIPUseCase @Inject constructor(
    private val ipBanRepository: IPBanRepository,
) {
    enum class FailureReason {
        IP_NOT_BANNED,
        INVALID_IP,
    }

    fun unbanIP(ip: String): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer().sanitizedIP(ip)

        val isValidIP = Regex.IP.matcher(sanitizedIP).matches()
        if (!isValidIP) {
            return Failure(FailureReason.INVALID_IP)
        }

        ipBanRepository.get(sanitizedIP)
            ?: return Failure(FailureReason.IP_NOT_BANNED)

        ipBanRepository.delete(sanitizedIP)

        return Success(Unit)
    }
}
