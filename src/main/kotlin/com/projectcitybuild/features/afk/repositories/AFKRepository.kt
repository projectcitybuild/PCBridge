package com.projectcitybuild.features.afk.repositories

import dagger.Reusable
import java.util.*
import javax.inject.Inject

@Reusable
class AFKRepository @Inject constructor() {
    private val afkPlayers: MutableSet<UUID> = mutableSetOf()

    fun isAFK(uuid: UUID): Boolean {
        return afkPlayers.contains(uuid)
    }

    fun add(uuid: UUID) {
        afkPlayers.add(uuid)
    }

    fun remove(uuid: UUID) {
        afkPlayers.remove(uuid)
    }
}