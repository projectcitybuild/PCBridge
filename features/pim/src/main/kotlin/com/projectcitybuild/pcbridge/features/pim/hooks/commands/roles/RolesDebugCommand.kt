package com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import org.bukkit.plugin.Plugin

class RolesDebugCommand(
    private val plugin: Plugin,
    private val permissions: Permissions,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal() = command("debug") {
        requiresPermission(PimPermissions.ROLES)
        argument("roles", StringArgumentType.greedyString()) {
            executesSuspending(commandTaskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(pimTracer) {
        val player = context.source.requirePlayer()

        val rolesArg = context.getArgument("roles", String::class.java)
        val roles = rolesArg.split(" ").toSet()
        check(roles.isNotEmpty()) { "No roles specified" }

        permissions.provider.setUserRoles(player.uniqueId, roles)

        player.sendMessage(l10n.pim.rolesSet(roles.joinToString(",")))
    }
}
