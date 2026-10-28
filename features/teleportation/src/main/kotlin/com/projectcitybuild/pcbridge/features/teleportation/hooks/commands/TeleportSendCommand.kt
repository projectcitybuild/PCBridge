package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args.withTeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportSendCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("send") {
        description = "Sends a teleport request to a player"
        requiresPermission(TeleportationPermissions.OTHERS)
        argument("player", SingleOnlinePlayerArgument(server)) {
            then(
                Commands
                    .argument("target", SingleOnlinePlayerArgument(server))
                    .withTeleportFlags(plugin) { context, flags ->
                        context.runCommandTracedSafely(teleportationTracer) {
                            application.send(
                                operator = context.source.sender,
                                player = context.getArgument("player", Player::class.java),
                                target = context.getArgument("target", Player::class.java),
                                flags = flags,
                            )
                        }
                    },
            )
        }
    }
}
