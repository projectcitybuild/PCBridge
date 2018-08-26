package com.projectcitybuild.core.extensions

import org.bukkit.entity.Player

fun Player.makeModel() : com.projectcitybuild.entities.models.Player {
    val model = com.projectcitybuild.entities.models.Player(uuid = this.uniqueId, isMuted = false)

    return model
}