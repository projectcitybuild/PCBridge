package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.entities.responses.GameBan
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.IPBanRepository
import java.net.SocketAddress
import java.util.*
import javax.inject.Inject

class AuthoriseConnectionUseCase @Inject constructor(
    private val banRepository: BanRepository,
    private val ipBanRepository: IPBanRepository,
) {
    sealed class Ban {
        data class UUID(val value: GameBan): Ban()
        data class IP(val value: IPBan): Ban()
    }

    @Throws(Exception::class)
    suspend fun getBan(uuid: UUID, ip: SocketAddress): Ban? {
        val uuidBan = banRepository.get(uuid)
        if (uuidBan != null) {
            return Ban.UUID(uuidBan)
        }

        val sanitizedIP = Sanitizer().sanitizedIP(ip.toString())
        val ipBan = ipBanRepository.get(sanitizedIP)
        if (ipBan != null) {
            return Ban.IP(ipBan)
        }

        return null
    }
}