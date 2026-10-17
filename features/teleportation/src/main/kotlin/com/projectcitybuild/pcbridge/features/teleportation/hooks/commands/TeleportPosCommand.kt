package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class TeleportPosCommand(
    private val plugin: Plugin,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("pos")
            .requiresPermission(PermissionNode.TELEPORT_POSITION)
            .then(
                withCoordinateArgs(plugin) { context, yaw, pitch ->
                    execute(context, yaw, pitch)
                },
            )
            .build()

    private suspend fun execute(
        context: PaperCommandContext,
        yaw: Float?,
        pitch: Float?,
    ) = context.scoped(teleportationTracer) {
        application.position(
            player = context.source.requirePlayer(),
            x = DoubleArgumentType.getDouble(context, "x"),
            y = DoubleArgumentType.getDouble(context, "y"),
            z = DoubleArgumentType.getDouble(context, "z"),
            yaw = yaw,
            pitch = pitch,
        )
    }
}
