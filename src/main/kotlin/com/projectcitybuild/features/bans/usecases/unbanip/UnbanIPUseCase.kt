package com.projectcitybuild.features.bans.usecases.unbanip

import com.projectcitybuild.core.utilities.Result

interface UnbanIPUseCase {
    enum class FailureReason {
        IP_NOT_BANNED,
        INVALID_IP,
    }
    fun unbanIP(ip: String): Result<Unit, FailureReason>
}