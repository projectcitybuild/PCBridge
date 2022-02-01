package com.projectcitybuild.features.bans.usecases.banip

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.modules.datetime.Time
import java.util.regex.Pattern
import javax.inject.Inject

class BanIPUseCaseImpl @Inject constructor(
    private val ipBanRepository: IPBanRepository,
    private val time: Time,
): BanIPUseCase {

    private val zeroTo255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
    private val ipRegex = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255"
    private val ipPattern = Pattern.compile(ipRegex)

    override fun banIP(
        ip: String,
        bannerName: String,
        reason: String?
    ): Result<Unit, BanIPUseCase.FailureReason> {
        val isValidIP = ipPattern.matcher(ip).matches()
        if (!isValidIP) {
            return Failure(BanIPUseCase.FailureReason.INVALID_IP)
        }
        try {
            val ban = IPBan(
                ip,
                bannerName,
                reason,
                createdAt = time.now(),
            )
            ipBanRepository.put(ban)
        } catch (e: IPBanRepository.IPAlreadyBanned) {
            return Failure(BanIPUseCase.FailureReason.IP_ALREADY_BANNED)
        }

        return Success(Unit)
    }
}