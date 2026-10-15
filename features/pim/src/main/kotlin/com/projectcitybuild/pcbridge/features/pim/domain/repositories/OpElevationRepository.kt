package com.projectcitybuild.pcbridge.features.pim.domain.repositories

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.OpElevateHttpService
import com.projectcitybuild.pcbridge.runtime.state.data.OpElevation
import com.projectcitybuild.pcbridge.runtime.state.data.toDomain
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
import java.util.UUID

class OpElevationRepository(
    private val opElevateHttpService: OpElevateHttpService,
    private val session: SessionStore,
) {
    fun get(playerUUID: UUID): OpElevation? =
        session.state.players[playerUUID]
            ?.syncedValue
            ?.opElevation

    suspend fun grant(
        playerUUID: UUID,
        reason: String,
    ): OpElevation {
        val elevation = opElevateHttpService.grant(playerUUID, reason).toDomain()
        update(playerUUID, elevation)
        return elevation
    }

    suspend fun revoke(playerUUID: UUID) {
        opElevateHttpService.revoke(playerUUID)
        update(playerUUID, null)
    }

    suspend fun expire(playerUUID: UUID) {
        update(playerUUID, null)
    }

    private suspend fun update(
        playerUUID: UUID,
        elevation: OpElevation?,
    ) {
        val playerSession = session.state.players[playerUUID] ?: return
        val synced = playerSession.syncedValue ?: return

        session.mutate { state ->
            val updated =
                playerSession.copy(
                    synced = synced.copy(opElevation = elevation),
                )
            state.copy(players = state.players + (playerUUID to updated))
        }
    }
}
