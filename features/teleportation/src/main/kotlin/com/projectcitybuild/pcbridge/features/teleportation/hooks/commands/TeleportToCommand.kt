package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.helpers.withTeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportToCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        val player =
            Commands.argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission(PermissionNode.TELEPORT_REQUEST)
                .withTeleportFlags(plugin) { context, flags -> execute(context, flags) }

        return Commands.literal("to")
            .then(player)
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
        flags: TeleportFlags,
    ) = context.scoped(teleportationTracer) {
        application.to(
            requester = context.source.requirePlayer(),
            target = context.getArgument("player", Player::class.java),
            flags = flags,
        )
    }
}
