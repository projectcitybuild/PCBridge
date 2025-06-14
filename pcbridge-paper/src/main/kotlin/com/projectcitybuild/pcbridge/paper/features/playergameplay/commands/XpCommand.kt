package com.projectcitybuild.pcbridge.paper.features.playergameplay.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.xp.XpSetCommand
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class XpCommand(
    private val plugin: Plugin,
    private val xpSetCommand: XpSetCommand,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode
        = Commands.literal("xp")
            .requiresPermission(PermissionNode.PLAYER_GAMEPLAY)
            .then(command = xpSetCommand)
            .build()
}
