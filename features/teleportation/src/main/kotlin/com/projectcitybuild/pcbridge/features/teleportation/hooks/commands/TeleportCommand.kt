package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.commands.then
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
    private val to: TeleportToCommand,
    private val here: TeleportHereCommand,
    private val send: TeleportSendCommand,
    private val all: TeleportAllCommand,
    private val position: TeleportPosCommand,
    private val random: TeleportRandomCommand,
    private val back: TeleportBackCommand,
    private val accept: TeleportAcceptCommand,
    private val deny: TeleportDenyCommand,
    private val cancel: TeleportCancelCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("tp")
            .executesSuspending(plugin) { context ->
                context.source.sender.sendMessage(l10n.teleportCommandHelp)
            }
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(server))
                    .requiresPermission(PermissionNode.TELEPORT_REQUEST)
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            application.to(
                                requester = context.source.requirePlayer(),
                                target = context.getArgument("player", Player::class.java),
                            )
                        }
                    },
            )
            .then(to)
            .then(here)
            .then(send)
            .then(all)
            .then(position)
            .then(random)
            .then(back)
            .then(accept)
            .then(deny)
            .then(cancel)
            .build()
}
