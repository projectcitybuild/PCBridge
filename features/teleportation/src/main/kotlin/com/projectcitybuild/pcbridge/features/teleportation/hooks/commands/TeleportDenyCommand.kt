package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

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

class TeleportDenyCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("deny") {
            description = "Declines a pending teleport request"
            argument("requester", SingleOnlinePlayerArgument(server)) {
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.deny(
                            context.source.requirePlayer(),
                            context.getArgument("requester", Player::class.java).uniqueId,
                        )
                    }
                }
            }
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    application.deny(context.source.requirePlayer(), requesterId = null)
                }
            }
        }
    }
}
