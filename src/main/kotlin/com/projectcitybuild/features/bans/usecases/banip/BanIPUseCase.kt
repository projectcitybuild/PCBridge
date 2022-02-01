package com.projectcitybuild.features.bans.usecases.banip

import com.projectcitybuild.core.utilities.Result

interface BanIPUseCase {
    enum class FailureReason {
        IP_ALREADY_BANNED,
        INVALID_IP,
    }
    fun banIP(ip: String, bannerName: String? = null, reason: String? = null): Result<Unit, FailureReason>
}
