package com.projectcitybuild.modules.players

import com.projectcitybuild.entities.CachedPlayer
import java.util.*

class PlayerRepository {
    fun get(uuid: UUID) : CachedPlayer {
        throw Exception()
    }

    fun save(player: CachedPlayer) {
        throw Exception()
    }
}