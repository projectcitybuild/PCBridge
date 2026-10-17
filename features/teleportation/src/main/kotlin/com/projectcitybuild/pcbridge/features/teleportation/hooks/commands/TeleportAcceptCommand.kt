package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
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

class TeleportAcceptCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("accept")
            .executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    application.accept(context.source.requirePlayer(), requesterId = null)
                }
            }
            .then(
                Commands.argument("requester", SingleOnlinePlayerArgument(server))
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            application.accept(
                                context.source.requirePlayer(),
                                context.getArgument("requester", Player::class.java).uniqueId,
                            )
                        }
                    },
            )
            .build()
}
