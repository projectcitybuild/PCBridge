package com.projectcitybuild.modules.sessioncache

import org.bukkit.entity.Player
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.util.*
import javax.inject.Inject

class SpigotSessionCache @Inject constructor() {
    val pendingJoinActions = HashMap<UUID, (Player, PlayerSpawnLocationEvent) -> Unit>()
}
