package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services

import com.projectcitybuild.pcbridge.http.pcb.models.OpElevation
import com.projectcitybuild.pcbridge.http.pcb.services.OpElevateHttpService
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class OpElevationService(
    private val opElevateHttpService: OpElevateHttpService,
    private val scheduler: OpElevationScheduler,
    private val server: Server,
) {
    suspend fun elevate(playerUUID: UUID, reason: String) {
        val elevation = opElevateHttpService.start(
            playerUUID = playerUUID,
            reason = reason,
        )
        val player = server.getPlayer(playerUUID) ?: run {
            log.warn { "Attempted to grant OP status but player not found" }
            return
        }
        restore(player, elevation)

        val remaining = elevation.remainingSeconds()
        player.sendRichMessage("OP granted (remaining: $remaining)")
    }

    fun restore(player: Player, elevation: OpElevation) {
        val remaining = elevation.remainingSeconds()
        if (remaining <= 0) {
            logSync.warn { "Attempted to grant OP status with an invalid duration ($remaining seconds)" }
            return
        }
        val playerUUID = player.uniqueId
        if (elevation.isActive()) {
            player.isOp = true
            logSync.info { "Granting OP status to ${player.name} ($playerUUID)" }
        }
        scheduler.schedule(
            playerUUID = playerUUID,
            duration = remaining.seconds,
            action = {
                server.getPlayer(playerUUID)?.let {
                    deop(it)
                    logSync.info { "OP status auto revoked for ${it.name} ($playerUUID)" }
                }
            }
        )
    }

    suspend fun revoke(playerUUID: UUID) {
        opElevateHttpService.end(playerUUID)
        scheduler.drop(playerUUID)

        val player = server.getPlayer(playerUUID)
        if (player == null) {
            log.warn { "Attempted to revoke OP status but player not found" }
            return
        }
        deop(player)
        logSync.info { "OP status manually revoked for ${player.name} (${player.uniqueId})" }
    }

    fun handleJoin(player: Player, elevation: OpElevation?) {
        val wasOp = player.isOp
        val isActive = elevation?.isActive() == true

        player.isOp = isActive

        when {
            isActive -> restore(player, elevation)
            wasOp -> player.sendRichMessage(l10n.opElevationRevoked)
        }
    }


    fun revokeSilently(playerUUID: UUID) {
        scheduler.drop(playerUUID)
    }

    fun isElevated(playerUuid: UUID): Boolean =
        scheduler.hasSchedule(playerUuid)


    private fun deop(player: Player) {
        player.isOp = false
        player.sendRichMessage("<gray>OP status revoked</gray>")
    }
}