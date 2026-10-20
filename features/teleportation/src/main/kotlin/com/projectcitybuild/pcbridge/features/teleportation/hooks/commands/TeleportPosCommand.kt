package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.plugin.Plugin

class TeleportPosCommand(
    private val plugin: Plugin,
    private val application: TeleportService,
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("pos") {
            description = "Teleports you to coordinates"
            requiresPermission(TeleportationPermissions.POSITION)
            brigadier {
                this
                    .then(
                        withCoordinateArgs(plugin) { context, yaw, pitch ->
                            execute(context, yaw, pitch)
                        },
                    )
            }
        }
    }

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
