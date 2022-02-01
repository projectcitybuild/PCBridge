package com.projectcitybuild.features.bans.usecases.authconnection

import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.entities.responses.GameBan
import java.net.SocketAddress
import java.util.*

interface AuthoriseConnectionUseCase {
    sealed class Ban {
        data class UUID(val value: GameBan): Ban()
        data class IP(val value: IPBan): Ban()
    }
    @Throws(Exception::class)
    suspend fun getBan(uuid: UUID, ip: SocketAddress): Ban?
}

