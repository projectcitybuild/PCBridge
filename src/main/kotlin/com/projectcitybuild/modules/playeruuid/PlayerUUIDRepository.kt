package com.projectcitybuild.modules.playeruuid

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.modules.proxyadapter.playerlist.OnlinePlayerList
import java.util.UUID
import javax.inject.Inject

open class PlayerUUIDRepository @Inject constructor(
    private val onlinePlayerList: OnlinePlayerList,
    private val mojangPlayerRepository: MojangPlayerRepository
) {
    suspend fun request(playerName: String): UUID? {
        val onlinePlayerUUID = onlinePlayerList.getUUID(playerName)
        if (onlinePlayerUUID != null) {
            return onlinePlayerUUID
        }
        return try {
            val mojangPlayer = mojangPlayerRepository.get(playerName = playerName)
            UUID.fromString(mojangPlayer.uuid.toDashFormattedUUID())
        } catch (e: MojangPlayerRepository.PlayerNotFoundException) {
            null
        }
    }
}