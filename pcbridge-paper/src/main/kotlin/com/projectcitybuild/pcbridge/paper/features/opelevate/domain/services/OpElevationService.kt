package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.support.java.humanReadable
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data.OpElevation
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.repositories.OpElevationRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

class OpElevationService(
    private val plugin: JavaPlugin,
    private val opElevationRepository: OpElevationRepository,
    private val opElevationScheduler: OpElevationScheduler,
    private val server: Server,
    private val localizedTime: LocalizedTime,
) {
    suspend fun elevate(playerUUID: UUID, reason: String) {
        val elevation = opElevationRepository.grant(playerUUID, reason)
        val player = server.getPlayer(playerUUID) ?: return
        applyElevation(player, elevation)
    }

    suspend fun revoke(playerUUID: UUID) {
        opElevationRepository.revoke(playerUUID)
        opElevationScheduler.cancel(playerUUID)

        server.getPlayer(playerUUID)
            ?.revokeOp(RevokeReason.MANUAL)
    }

    fun handleJoin(player: Player) {
        val elevation = opElevationRepository.get(player.uniqueId)
        if (elevation == null) {
            if (player.isOp) {
                logSync.debug { "Revoking OP status due to desync" }
                player.revokeOp(RevokeReason.DESYNC)
            }
            return
        }
        applyElevation(player, elevation)
    }

    fun handleLeave(playerUUID: UUID) {
        opElevationScheduler.cancel(playerUUID)
    }

    fun elevation(playerUUID: UUID): OpElevation? =
        opElevationRepository.get(playerUUID)

    private fun applyElevation(player: Player, elevation: OpElevation) {
        val now = localizedTime.nowInstant()
        val remaining = elevation.remainingAt(now)
        if (remaining == null) {
            player.revokeOp(RevokeReason.EXPIRED)
            opElevationScheduler.cancel(player.uniqueId)
            return
        }
        player.grantOp(duration = remaining)

        val playerUUID = player.uniqueId
        opElevationScheduler.schedule(playerUUID, remaining.toKotlinDuration()) {
            withContext(plugin.minecraftDispatcher) {
                server.getPlayer(playerUUID)
                    ?.revokeOp(RevokeReason.EXPIRED)
            }
            opElevationRepository.expire(playerUUID)
        }
    }

    enum class RevokeReason {
        EXPIRED,
        MANUAL,
        DESYNC,
    }
}

private fun Player.grantOp(duration: Duration) {
    isOp = true
    sendRichMessage(l10n.opElevationGranted(duration.humanReadable()))
    logSync.info { "Granted OP status to $name ($uniqueId)" }
}

private fun Player.revokeOp(reason: OpElevationService.RevokeReason) {
    if (!isOp) return
    isOp = false

    val message = when (reason) {
        OpElevationService.RevokeReason.MANUAL,
        OpElevationService.RevokeReason.DESYNC
            -> l10n.opElevationRevoked
        OpElevationService.RevokeReason.EXPIRED
            -> l10n.opElevationExpired
    }
    sendRichMessage(message)

    logSync.info { "Revoked OP status (reason: $reason) from $name ($uniqueId)" }
}