package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportBackCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("back")
        .executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                val player = context.source.requirePlayer()
                application.back(player, player)
            }
        }.then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission(TeleportationPermissions.BACK_OTHERS)
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.back(
                            operator = context.source.sender,
                            target = context.getArgument("player", Player::class.java),
                        )
                    }
                },
        ).build()
}
