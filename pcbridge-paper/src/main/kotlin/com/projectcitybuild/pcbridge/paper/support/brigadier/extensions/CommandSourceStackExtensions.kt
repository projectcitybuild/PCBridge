package com.projectcitybuild.pcbridge.paper.support.brigadier.extensions

import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

fun CommandSourceStack.requirePlayer(): Player {
    val player = sender as? Player
    checkNotNull(player) { "<red>Only players can use this command</red>" }
    return player
}