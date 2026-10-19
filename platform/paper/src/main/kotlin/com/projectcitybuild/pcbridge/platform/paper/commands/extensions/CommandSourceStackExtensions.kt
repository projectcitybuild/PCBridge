package com.projectcitybuild.pcbridge.platform.paper.commands.extensions

import com.projectcitybuild.pcbridge.core.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

fun CommandSourceStack.requirePlayer(): Player {
    val player = sender as? Player
    checkNotNull(player) { l10n.errors.errorOnlyPlayersCanUseThisCommand }
    return player
}
