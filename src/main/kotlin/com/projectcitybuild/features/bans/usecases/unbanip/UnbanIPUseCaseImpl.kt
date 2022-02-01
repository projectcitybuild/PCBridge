package com.projectcitybuild.features.bans.usecases.unbanip

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import java.util.regex.Pattern
import javax.inject.Inject

class UnbanIPUseCaseImpl @Inject constructor(
    private val ipBanRepository: IPBanRepository,
): UnbanIPUseCase {

    private val zeroTo255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
    private val ipRegex = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255"
    private val ipPattern = Pattern.compile(ipRegex)

    override fun unbanIP(ip: String): Result<Unit, UnbanIPUseCase.FailureReason> {
        val isValidIP = ipPattern.matcher(ip).matches()
        if (!isValidIP) {
            return Failure(UnbanIPUseCase.FailureReason.INVALID_IP)
        }

        ipBanRepository.get(ip)
            ?: return Failure(UnbanIPUseCase.FailureReason.IP_NOT_BANNED)

        ipBanRepository.delete(ip)

        return Success(Unit)
    }
}