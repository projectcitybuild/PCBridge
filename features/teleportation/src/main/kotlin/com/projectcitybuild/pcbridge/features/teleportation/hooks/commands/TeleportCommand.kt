package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
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
    override val commandDefinition by lazy {
        command("tp") {
            description = "Teleports a player to a target"
            argument("player", SingleOnlinePlayerArgument(server)) {
                requires { context ->
                    context.sender.hasPermission(TeleportationPermissions.REQUEST.node) ||
                        context.sender.hasPermission("essentials.tp")
                }
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.to(
                            requester = context.source.requirePlayer(),
                            target = context.getArgument("player", Player::class.java),
                        )
                    }
                }
            }
            then(to)
            then(here)
            then(send)
            then(all)
            then(position)
            then(random)
            then(back)
            then(accept)
            then(deny)
            then(cancel)
        }
    }
}
