package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportAcceptCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("accept") {
        description = "Accepts a pending teleport request"
        argument("requester", SingleOnlinePlayerArgument(server)) {
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    application.accept(
                        target = context.source.requirePlayer(),
                        requesterId = context.getArgument("requester", Player::class.java).uniqueId,
                    )
                }
            }
        }
        executesSuspending(plugin) { context ->
            context.runCommandTracedSafely(teleportationTracer) {
                application.accept(
                    target = context.source.requirePlayer(),
                    requesterId = null,
                )
            }
        }
    }
}
