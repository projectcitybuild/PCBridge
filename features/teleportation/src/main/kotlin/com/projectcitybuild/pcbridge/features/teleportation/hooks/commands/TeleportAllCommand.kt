package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args.withTeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportAllCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("all") {
            description = "Teleports all players to a target"
            requiresPermission(TeleportationPermissions.ALL)
            brigadier {
                withTeleportFlags(plugin) { context, flags ->
                    execute(context, targetArgument = null, flags = flags)
                }
            }
            argument("target", SingleOnlinePlayerArgument(server)) {
                withTeleportFlags(plugin) { context, flags ->
                    execute(context, "target", flags)
                }
            }
        }
    }

    private suspend fun execute(
        context: PaperCommandContext,
        targetArgument: String?,
        flags: TeleportFlags,
    ) = context.scoped(teleportationTracer) {
        val target = targetArgument?.let {
            context.getArgument(it, Player::class.java)
        }
        application.all(
            operator = context.source.sender,
            requestedTarget = target,
            flags = flags,
        )
    }
}
