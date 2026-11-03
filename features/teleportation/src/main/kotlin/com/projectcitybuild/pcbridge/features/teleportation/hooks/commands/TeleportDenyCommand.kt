package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class TeleportDenyCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("deny") {
        description = "Declines a pending teleport request"
        argument(Argument.onlinePlayer("requester", server)) {
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    application.deny(
                        context.source.requirePlayer(),
                        Argument.onlinePlayer("requester", server).get(context).uniqueId,
                    )
                }
            }
        }
        executesSuspending(plugin) { context ->
            context.runCommandTracedSafely(teleportationTracer) {
                application.deny(context.source.requirePlayer(), requesterId = null)
            }
        }
    }
}
