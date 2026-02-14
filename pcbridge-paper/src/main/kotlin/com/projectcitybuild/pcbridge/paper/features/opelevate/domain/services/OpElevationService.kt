package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services

import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data.OpElevation
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.repositories.OpElevationRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import org.bukkit.Server
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import kotlin.time.toKotlinDuration

class OpElevationService(
    private val opElevationRepository: OpElevationRepository,
    private val scheduler: OpElevationScheduler,
    private val server: Server,
    private val localizedTime: LocalizedTime,
) {
    suspend fun elevate(playerUUID: UUID, reason: String) {
        val elevation = opElevationRepository.grant(playerUUID, reason)

        val player = server.getPlayer(playerUUID) ?: run {
            log.warn { "Attempted to grant OP status but player not found" }
            return
        }
        val now = localizedTime.nowInstant()
        val transition = elevation.transition(now)
        applyTransition(player, transition)
    }

    suspend fun revoke(playerUUID: UUID) {
        opElevationRepository.revoke(playerUUID)
        scheduler.cancel(playerUUID)

        val player = server.getPlayer(playerUUID)
        player?.revokeOp(reason = RevokeReason.MANUAL)
    }

    fun handleJoin(player: Player) {
        val now = localizedTime.nowInstant()
        val elevation = opElevationRepository.get(player.uniqueId)
        if (elevation == null) {
            if (player.isOp) player.revokeOp(reason = RevokeReason.DESYNC)
            return
        }
        applyTransition(player, elevation.transition(now))
    }

    fun handleLeave(playerUUID: UUID) {
        scheduler.cancel(playerUUID)
    }

    fun isElevated(playerUUID: UUID): Boolean =
        scheduler.has(playerUUID)

    private fun applyTransition(
        player: Player,
        transition: OpElevation.Transition
    ) {
        when (transition) {
            is OpElevation.Transition.Grant ->
                grant(player, transition.remaining)

            OpElevation.Transition.Expire ->
                player.revokeOp(reason = RevokeReason.EXPIRED)
        }
    }

    private fun grant(player: Player, remaining: Duration) {
        if (!remaining.isPositive) return

        val playerUUID = player.uniqueId

        player.grantOp(duration = remaining)

        scheduler.schedule(
            playerUUID = playerUUID,
            duration = remaining.toKotlinDuration(),
        ) {
            // TODO: ensure we're on main thread
            val player = server.getPlayer(playerUUID)
            player?.revokeOp(reason = RevokeReason.EXPIRED)
        }
    }

    enum class RevokeReason {
        EXPIRED,
        MANUAL,
        DESYNC
    }
}

private fun Player.grantOp(duration: Duration) {
    isOp = true
    sendRichMessage("OP granted (remaining: ${duration.seconds} seconds)")
    logSync.info { "Granted OP status to $name ($uniqueId)" }
}

private fun Player.revokeOp(reason: OpElevationService.RevokeReason) {
    if (!isOp) return
    isOp = false
    sendRichMessage(l10n.opElevationRevoked)
    logSync.info { "Revoked OP status (reason: $reason) from $name ($uniqueId)" }
}
