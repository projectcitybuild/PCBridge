package com.projectcitybuild.pcbridge.platform.paper.commands.arguments

import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import org.bukkit.plugin.Plugin

fun CommandBuilder.withCoordinateArgs(
    plugin: Plugin,
    execute: suspend (PaperCommandContext, Double, Double, Double, Float?, Float?) -> Unit,
) {
    val xArg = Argument.double("x")
    val yArg = Argument.double("y")
    val zArg = Argument.double("z")
    val yawArg = Argument.float("yaw")
    val pitchArg = Argument.float("pitch")

    argument(xArg) {
        argument(yArg) {
            argument(zArg) {
                executesSuspending(plugin) { context ->
                    execute(
                        context,
                        xArg.get(context),
                        yArg.get(context),
                        zArg.get(context),
                        null,
                        null,
                    )
                }
                argument(yawArg) {
                    executesSuspending(plugin) { context ->
                        execute(
                            context,
                            xArg.get(context),
                            yArg.get(context),
                            zArg.get(context),
                            yawArg.get(context),
                            null,
                        )
                    }
                    argument(pitchArg) {
                        executesSuspending(plugin) { context ->
                            execute(
                                context,
                                xArg.get(context),
                                yArg.get(context),
                                zArg.get(context),
                                yawArg.get(context),
                                pitchArg.get(context),
                            )
                        }
                    }
                }
            }
        }
    }
}
