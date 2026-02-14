package com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.features.sync.syncTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class RolesDebugCommand(
    private val plugin: Plugin,
    private val permissions: Permissions,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("debug")
            .requiresPermission(PermissionNode.PIM_ROLES)
            .then(
                Commands.argument("groups", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(syncTracer) {
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