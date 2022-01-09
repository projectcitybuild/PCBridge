package com.projectcitybuild.modules.sessioncache

import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.*

class SessionCache {
    val afkPlayerList: MutableList<UUID> = mutableListOf()
    val pendingJoinActions = HashMap<UUID, PendingJoinAction>()
}

sealed class PendingJoinAction {
    class TeleportToLocation(val location: Location): PendingJoinAction()
    class TeleportToPlayer(val targetUUID: UUID): PendingJoinAction()
}