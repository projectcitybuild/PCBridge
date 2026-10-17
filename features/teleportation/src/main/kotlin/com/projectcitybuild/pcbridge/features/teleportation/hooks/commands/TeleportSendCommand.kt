package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args.withTeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportSendCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("send")
            .requiresPermission(PermissionNode.TELEPORT_OTHERS)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(server))
                    .then(
                        Commands.argument("target", SingleOnlinePlayerArgument(server))
                            .withTeleportFlags(plugin) { context, flags ->
                                context.scoped(teleportationTracer) {
                                    application.send(
                                        operator = context.source.sender,
                                        player = context.getArgument("player", Player::class.java),
                                        target = context.getArgument("target", Player::class.java),
                                        flags = flags,
                                    )
                                }
                            },
                    ),
            )
            .build()
}
