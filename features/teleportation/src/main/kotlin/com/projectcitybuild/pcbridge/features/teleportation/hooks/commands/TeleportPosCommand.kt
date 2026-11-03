package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.plugin.Plugin

class TeleportPosCommand(
    private val plugin: Plugin,
    private val application: TeleportService,
) : BrigadierCommand {
    override val builder = command("pos") {
        description = "Teleports you to coordinates"
        requiresPermission(TeleportationPermissions.POSITION)
        withCoordinateArgs(plugin) { context, x, y, z, yaw, pitch ->
            execute(context, x, y, z, yaw, pitch)
        }
    }

    private suspend fun execute(
        context: PaperCommandContext,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float?,
        pitch: Float?,
    ) = context.runCommandTracedSafely(teleportationTracer) {
        application.position(
            player = context.source.requirePlayer(),
            x = x,
            y = y,
            z = z,
            yaw = yaw,
            pitch = pitch,
        )
    }
}
