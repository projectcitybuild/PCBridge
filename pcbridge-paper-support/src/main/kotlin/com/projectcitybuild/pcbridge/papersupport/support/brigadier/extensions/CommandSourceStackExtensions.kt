package com.projectcitybuild.pcbridge.papersupport.support.brigadier.extensions

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

fun CommandSourceStack.requirePlayer(): Player {
    val player = sender as? Player
    checkNotNull(player) { "<red>Only players can use this command</red>" }
    return player
}