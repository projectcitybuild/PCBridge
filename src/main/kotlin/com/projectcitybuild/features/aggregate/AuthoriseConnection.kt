package com.projectcitybuild.features.aggregate

import com.projectcitybuild.entities.responses.Aggregate
import com.projectcitybuild.entities.responses.IPBan
import com.projectcitybuild.entities.responses.PlayerBan
import javax.inject.Inject

class AuthoriseConnection @Inject constructor() {
    sealed class ConnectResult {
        object Allowed : ConnectResult()
        data class Denied(val ban: Ban) : ConnectResult()
    }

    sealed class Ban {
        data class UUID(val value: PlayerBan) : Ban()
        data class IP(val value: IPBan) : Ban()
    }

    @Throws(Exception::class)
    fun execute(aggregate: Aggregate): ConnectResult {
        val ban = getBan(aggregate)
        if (ban != null) {
            return ConnectResult.Denied(ban = ban)
        }

        return ConnectResult.Allowed
    }

    private fun getBan(aggregate: Aggregate): Ban? {
        if (aggregate.playerBan !== null && aggregate.playerBan.isActive) {
            return Ban.UUID(aggregate.playerBan)
        }
        if (aggregate.ipBan !== null && aggregate.ipBan.isActive) {
            return Ban.IP(aggregate.ipBan)
        }
        return null
    }
}
