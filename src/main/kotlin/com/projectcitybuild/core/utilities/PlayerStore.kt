package com.projectcitybuild.core.utilities

import com.projectcitybuild.entities.models.Player
import java.util.*

class PlayerStore {
    interface PlayerStoreDelegate {
        fun onStoreUpdate(player: Player)
    }

    private val store = mutableMapOf<UUID, Player>()
    var delegate: PlayerStoreDelegate? = null

    fun get(uuid: UUID) : Player? {
        return store.get(uuid)
    }

    fun put(uuid: UUID, player: Player) {
        store[uuid] = player
        delegate?.onStoreUpdate(player)
    }

    fun remove(uuid: UUID) {
        store.remove(uuid)
    }

    fun clear() {
        store.clear()
    }

}