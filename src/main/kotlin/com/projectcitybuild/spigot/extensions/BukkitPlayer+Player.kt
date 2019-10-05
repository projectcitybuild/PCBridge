package com.projectcitybuild.spigot.extensions

import org.bukkit.entity.Player

fun Player.makeModel() : com.projectcitybuild.entities.Player {
    return com.projectcitybuild.entities.Player(
            uuid = this.uniqueId,
            isMuted = false
    )
}
