package com.projectcitybuild.pcbridge.paper.features.sync.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class SyncDebugCommand(
    private val plugin: Plugin,
    private val permissions: Permissions,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("syncdebug")
            .requiresPermission(PermissionNode.PLAYER_SYNC_DEBUG)
            .then(
                Commands.argument("groups", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()

        val groupsArg = context.getArgument("groups", String::class.java)
        val groups = groupsArg.split(" ").toSet()
        check(groups.isNotEmpty()) { "No groups specified" }

        permissions.provider.setUserRoles(player.uniqueId, groups)

        player.sendRichMessage(
            "<red>Your groups have been set to ${groups.joinToString(",")}</red>\n" +
            "<gray>Use /sync or reconnect to revert this</gray>"
        )
    }
}
