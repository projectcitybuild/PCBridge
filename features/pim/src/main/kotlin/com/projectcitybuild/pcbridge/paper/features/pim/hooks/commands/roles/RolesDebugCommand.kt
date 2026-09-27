package com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.pim.pimTracer
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import com.projectcitybuild.pcbridge.paper.runtime.permissions.Permissions
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
                Commands.argument("roles", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(pimTracer) {
            val player = context.source.requirePlayer()

            val rolesArg = context.getArgument("roles", String::class.java)
            val roles = rolesArg.split(" ").toSet()
            check(roles.isNotEmpty()) { "No roles specified" }

            permissions.provider.setUserRoles(player.uniqueId, roles)

            player.sendRichMessage(
                "<red>Your roles have been set to ${roles.joinToString(",")}</red>\n" +
                    "<gray>Use /sync or reconnect to revert this</gray>",
            )
        }
}
