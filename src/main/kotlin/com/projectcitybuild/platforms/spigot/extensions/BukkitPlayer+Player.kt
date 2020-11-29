package com.projectcitybuild.platforms.spigot.extensions

import org.bukkit.entity.Player

fun Player.makeModel() : com.projectcitybuild.core.entities.Player {
    return com.projectcitybuild.core.entities.Player(
            uuid = this.uniqueId,
            isMuted = false
    )
}
