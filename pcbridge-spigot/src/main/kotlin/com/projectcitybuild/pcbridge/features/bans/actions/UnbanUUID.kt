package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerUUIDRepository
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Result
import com.projectcitybuild.pcbridge.utils.Success
import java.util.UUID

class UnbanUUID(
    private val playerBanRepository: PlayerBanRepository,
    private val playerUUIDRepository: PlayerUUIDRepository,
) {
    enum class FailureReason {
        PlayerDoesNotExist,
        PlayerNotBanned,
    }

    suspend fun unban(
        targetPlayerName: String,
        unbannerUUID: UUID?,
    ): Result<Unit, FailureReason> {
        try {
            val targetPlayerUUID =
                playerUUIDRepository.get(targetPlayerName)
                    ?: return Failure(FailureReason.PlayerDoesNotExist)

            playerBanRepository.unban(
                targetPlayerUUID = targetPlayerUUID,
                unbannerUUID = unbannerUUID,
            )

            return Success(Unit)
        } catch (e: UUIDBanHttpService.UUIDNotBannedException) {
            return Failure(FailureReason.PlayerNotBanned)
        }
    }
}
