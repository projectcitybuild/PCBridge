package com.projectcitybuild.modules.sessioncache

import org.bukkit.entity.Player
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.util.*

class SpigotSessionCache {
    val pendingJoinActions = HashMap<UUID, (Player, PlayerSpawnLocationEvent) -> Unit>()
}
