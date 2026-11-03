package com.projectcitybuild.pcbridge.platform.paper.commands.extensions

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.l10n.checkNotNullLocalized
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

fun CommandSourceStack.requirePlayer(): Player {
    return checkNotNullLocalized(sender as? Player) {
        l10n.errors.errorOnlyPlayersCanUseThisCommand
    }
}
