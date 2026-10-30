package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportBackCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("back") {
        description = "Returns you to your previous location"
        argument("player", OnlinePlayerArgument(server)) {
            requiresPermission(TeleportationPermissions.BACK_OTHERS)
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    application.back(
                        operator = context.source.sender,
                        target = context.getArgument("player", Player::class.java),
                    )
                }
            }
        }
        executesSuspending(plugin) { context ->
            context.runCommandTracedSafely(teleportationTracer) {
                val player = context.source.requirePlayer()
                application.back(player, player)
            }
        }
    }
}
