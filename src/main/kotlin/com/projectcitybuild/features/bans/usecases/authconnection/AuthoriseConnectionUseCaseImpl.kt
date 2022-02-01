package com.projectcitybuild.features.bans.usecases.authconnection

import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import java.net.SocketAddress
import java.util.*
import javax.inject.Inject

class AuthoriseConnectionUseCaseImpl @Inject constructor(
    private val banRepository: BanRepository,
    private val ipBanRepository: IPBanRepository,
): AuthoriseConnectionUseCase {

    override suspend fun getBan(uuid: UUID, ip: SocketAddress): AuthoriseConnectionUseCase.Ban? {
        val uuidBan = banRepository.get(uuid)
        if (uuidBan != null) {
            return AuthoriseConnectionUseCase.Ban.UUID(uuidBan)
        }

        val sanitizedIP = Sanitizer().sanitizedIP(ip.toString())
        val ipBan = ipBanRepository.get(sanitizedIP)
        if (ipBan != null) {
            return AuthoriseConnectionUseCase.Ban.IP(ipBan)
        }

        return null
    }
}