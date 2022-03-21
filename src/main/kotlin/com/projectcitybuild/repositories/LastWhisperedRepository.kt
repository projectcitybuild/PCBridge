package com.projectcitybuild.repositories

import dagger.Reusable
import java.util.UUID
import javax.inject.Inject

@Reusable
class LastWhisperedRepository @Inject constructor() {
    private val lastWhispered = HashMap<UUID, UUID>()

    fun set(whisperer: UUID, targetOfWhisper: UUID) {
        lastWhispered[targetOfWhisper] = whisperer
    }

    fun remove(playerUUID: UUID) {
        lastWhispered.remove(playerUUID)
    }

    fun getLastWhisperer(playerUUID: UUID): UUID? {
        return lastWhispered[playerUUID]
    }
}
