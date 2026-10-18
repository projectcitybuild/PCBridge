package com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

fun withCoordinateArgs(
    plugin: Plugin,
    execute: suspend (PaperCommandContext, Float?, Float?) -> Unit,
): RequiredArgumentBuilder<CommandSourceStack, Double> {
    val yawPitch = Commands.argument("yaw", FloatArgumentType.floatArg())
        .executesSuspending(plugin) { context ->
            execute(context, FloatArgumentType.getFloat(context, "yaw"), null)
        }
        .then(
            Commands.argument("pitch", FloatArgumentType.floatArg())
                .executesSuspending(plugin) { context ->
                    execute(
                        context,
                        FloatArgumentType.getFloat(context, "yaw"),
                        FloatArgumentType.getFloat(context, "pitch"),
                    )
                }
        )

    return Commands.argument("x", DoubleArgumentType.doubleArg())
        .then(
            Commands.argument("y", DoubleArgumentType.doubleArg())
                .then(
                    Commands.argument("z", DoubleArgumentType.doubleArg())
                        .executesSuspending(plugin) { execute(it, null, null) }
                        .then(yawPitch)
                )
        )
}
