package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args.withTeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportAllCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        val command =
            Commands.literal("all")
                .requiresPermission(PermissionNode.TELEPORT_ALL)
                .withTeleportFlags(plugin) { context, flags ->
                    execute(context, targetArgument = null, flags = flags)
                }

        command.then(
            Commands.argument("target", SingleOnlinePlayerArgument(server))
                .withTeleportFlags(plugin) { context, flags ->
                    execute(context, "target", flags)
                },
        )
        return command.build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
        targetArgument: String?,
        flags: TeleportFlags,
    ) = context.scoped(teleportationTracer) {
        val target = targetArgument?.let {
            context.getArgument(it, Player::class.java)
        }
        application.all(
            operator = context.source.sender,
            requestedTarget = target,
            flags = flags,
        )
    }
}
