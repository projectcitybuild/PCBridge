package com.projectcitybuild.old_modules.sessioncache

import org.bukkit.Location
import java.util.*

class SessionCache {
    val afkPlayerList: MutableList<UUID> = mutableListOf()
    val pendingJoinActions = HashMap<UUID, PendingJoinAction>()
}

sealed class PendingJoinAction {
    class TeleportToLocation(val location: Location): PendingJoinAction()
    class TeleportToPlayer(val targetUUID: UUID): PendingJoinAction()
}