package com.projectcitybuild.features.bans.actions

import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.features.bans.isValidIP
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.event.player.PlayerKickEvent
import java.util.UUID

class BanIP(
    private val ipBanRepository: IPBanRepository,
    private val server: Server,
) {
    enum class FailureReason {
        IP_ALREADY_BANNED,
        INVALID_IP,
    }

    suspend fun execute(
        ip: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String,
    ): Result<Unit, FailureReason> {
        val sanitizedIP = Sanitizer.sanitizedIP(ip)
        if (!isValidIP(sanitizedIP)) {
            return Failure(FailureReason.INVALID_IP)
        }
        try {
            ipBanRepository.ban(
                ip = sanitizedIP,
                bannerUUID = bannerUUID,
                bannerName = bannerName,
                reason = reason,
            )
        } catch (e: IPBanHttpService.IPAlreadyBannedException) {
            return Failure(FailureReason.IP_ALREADY_BANNED)
        }

        server.onlinePlayers
            .firstOrNull { it.address.toString() == ip }
            ?.kick(
                Component.text("You have been banned.\n\nAppeal @ projectcitybuild.com"),
                PlayerKickEvent.Cause.BANNED,
            )

        return Success(Unit)
    }
}
