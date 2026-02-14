package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.services.OpElevateHttpService
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data.OpElevation
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data.toDomain
import java.util.UUID

class OpElevationRepository(
    private val opElevateHttpService: OpElevateHttpService,
    private val session: SessionStore,
) {
    fun get(playerUUID: UUID): OpElevation? {
        val playerSession = session.state.players[playerUUID]
        if (playerSession == null) {
            logSync.warn { "Player session was null when looking up op elevation" }
        }
        return playerSession?.syncedValue?.opElevation
    }

    suspend fun grant(playerUUID: UUID, reason: String): OpElevation {
        val response = opElevateHttpService.grant(playerUUID, reason)
        val elevation = response.toDomain()
        updatePlayerSession(playerUUID, elevation = elevation)
        return elevation
    }

    suspend fun revoke(playerUUID: UUID): OpElevation {
        val response = opElevateHttpService.revoke(playerUUID)
        val elevation = response.toDomain()
        updatePlayerSession(playerUUID, elevation = null)
        return elevation
    }

    private suspend fun updatePlayerSession(
        playerUUID: UUID,
        elevation: OpElevation?,
    ) {
        val playerSession = session.state.players[playerUUID]
        val synced = playerSession?.syncedValue
        if (synced == null) {
            log.warn { "Unable to store OP elevation: player synced data not found" }
            return
        }
        session.mutate { state ->
            val updatedSession = playerSession.copy(
                synced = synced.copy(opElevation = elevation),
            )
            state.copy(players = state.players + mapOf(playerUUID to updatedSession))
        }
    }
}