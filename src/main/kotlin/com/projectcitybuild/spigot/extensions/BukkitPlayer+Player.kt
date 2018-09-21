package com.projectcitybuild.spigot.extensions

import org.bukkit.entity.Player

fun Player.makeModel() : com.projectcitybuild.entities.models.Player {
    return com.projectcitybuild.entities.models.Player(uuid = this.uniqueId, isMuted = false)
}