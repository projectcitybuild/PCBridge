package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args.withTeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportHereCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("here") {
        description = "Teleports a target player to you"
        requiresPermission(TeleportationPermissions.OTHERS)
        argument("player", OnlinePlayerArgument(server)) {
            withTeleportFlags(plugin) { context, flags -> execute(context, flags) }
        }
    }

    private suspend fun execute(
        context: PaperCommandContext,
        flags: TeleportFlags,
    ) = context.runCommandTracedSafely(teleportationTracer) {
        application.here(
            operator = context.source.requirePlayer(),
            target = context.getArgument("player", Player::class.java),
            flags = flags,
        )
    }
}
