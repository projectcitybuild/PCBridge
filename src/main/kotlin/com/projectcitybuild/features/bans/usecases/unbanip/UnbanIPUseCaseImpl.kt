package com.projectcitybuild.features.bans.usecases.unbanip

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import javax.inject.Inject

class UnbanIPUseCaseImpl @Inject constructor(
    private val ipBanRepository: IPBanRepository,
): UnbanIPUseCase {

    override fun unbanIP(ip: String): Result<Unit, UnbanIPUseCase.FailureReason> {
        val sanitizedIP = Sanitizer.sanitizedIP(ip)

        val isValidIP = Regex.IP.matcher(sanitizedIP).matches()
        if (!isValidIP) {
            return Failure(UnbanIPUseCase.FailureReason.INVALID_IP)
        }

        ipBanRepository.get(sanitizedIP)
            ?: return Failure(UnbanIPUseCase.FailureReason.IP_NOT_BANNED)

        ipBanRepository.delete(sanitizedIP)

        return Success(Unit)
    }
}