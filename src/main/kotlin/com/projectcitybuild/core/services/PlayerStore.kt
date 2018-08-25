package com.projectcitybuild.core.services

import com.projectcitybuild.entities.models.Player
import java.util.*

class PlayerStore {
    private val store = mutableMapOf<UUID, Player>()

    fun get(uuid: UUID) : Player? {
        return store.get(uuid)
    }

    fun put(uuid: UUID, player: Player) {
        store[uuid] = player
    }

    fun remove(uuid: UUID) {
        store.remove(uuid)
    }

    fun clear() {
        store.clear()
    }
}