package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.features.teleportation.hooks.arguments.withTeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class TeleportSendCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("send") {
        description = "Sends a teleport request to a player"
        requiresPermission(TeleportationPermissions.OTHERS)
        argument(Argument.onlinePlayer("player", server)) {
            argument(Argument.onlinePlayer("target", server)) {
                withTeleportFlags(plugin) { context, flags ->
                    context.runCommandTracedSafely(teleportationTracer) {
                        application.send(
                            operator = context.source.sender,
                            player = Argument.onlinePlayer("player", server).get(context),
                            target = Argument.onlinePlayer("target", server).get(context),
                            flags = flags,
                        )
                    }
                }
            }
        }
    }
}
