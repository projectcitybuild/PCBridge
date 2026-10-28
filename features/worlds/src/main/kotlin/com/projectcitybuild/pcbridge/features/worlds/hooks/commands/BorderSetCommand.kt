package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderGeometry
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import org.bukkit.plugin.Plugin

class BorderSetCommand(
    private val plugin: Plugin,
    private val worldBorderService: WorldBorderService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("set") {
        description = l10n.worlds.borderSetCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        literal("square") {
            radiusArguments { x, z, radius -> WorldBorder.Square(x, z, radius) }
        }
        literal("circle") {
            radiusArguments { x, z, radius -> WorldBorder.Circle(x, z, radius) }
        }
        literal("rectangle") {
            rectangleArguments()
        }
    }

    private fun CommandBuilder.radiusArguments(createBorder: (Double, Double, Double) -> WorldBorder) {
        argument("radius", DoubleArgumentType.doubleArg(1.0, WorldBorderGeometry.MAX_RADIUS)) {
            executesSuspending(taskExecutor) { context ->
                set(
                    context = context,
                    createBorder = { centerX, centerZ ->
                        createBorder(
                            centerX,
                            centerZ,
                            DoubleArgumentType.getDouble(context, "radius"),
                        )
                    },
                )
            }
            argument("x", DoubleArgumentType.doubleArg()) {
                argument("z", DoubleArgumentType.doubleArg()) {
                    executesSuspending(taskExecutor) { context ->
                        set(
                            context = context,
                            centerX = DoubleArgumentType.getDouble(context, "x"),
                            centerZ = DoubleArgumentType.getDouble(context, "z"),
                            createBorder = { x, z ->
                                createBorder(
                                    x,
                                    z,
                                    DoubleArgumentType.getDouble(context, "radius"),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private fun CommandBuilder.rectangleArguments() {
        argument("width", DoubleArgumentType.doubleArg(1.0, WorldBorderGeometry.MAX_DIMENSION)) {
            argument("depth", DoubleArgumentType.doubleArg(1.0, WorldBorderGeometry.MAX_DIMENSION)) {
                executesSuspending(taskExecutor) { context ->
                    set(
                        context = context,
                        createBorder = { centerX, centerZ ->
                            WorldBorder.Rectangle(
                                centerX,
                                centerZ,
                                DoubleArgumentType.getDouble(context, "width"),
                                DoubleArgumentType.getDouble(context, "depth"),
                            )
                        },
                    )
                }
                argument("x", DoubleArgumentType.doubleArg()) {
                    argument("z", DoubleArgumentType.doubleArg()) {
                        executesSuspending(taskExecutor) { context ->
                            set(
                                context = context,
                                centerX = DoubleArgumentType.getDouble(context, "x"),
                                centerZ = DoubleArgumentType.getDouble(context, "z"),
                                createBorder = { x, z ->
                                    WorldBorder.Rectangle(
                                        x,
                                        z,
                                        DoubleArgumentType.getDouble(context, "width"),
                                        DoubleArgumentType.getDouble(context, "depth"),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun set(
        context: PaperCommandContext,
        createBorder: (Double, Double) -> WorldBorder,
        centerX: Double? = null,
        centerZ: Double? = null,
    ) {
        val player = context.source.requirePlayer()
        val border = try {
            createBorder(
                centerX ?: player.location.x,
                centerZ ?: player.location.z,
            )
        } catch (failure: IllegalArgumentException) {
            player.sendMessage(l10n.worlds.invalidBorder(failure.message))
            return
        }

        val relocationFailures = worldBorderService.set(player.world, border)
        val message =
            when (border) {
                is WorldBorder.Rectangle -> {
                    l10n.worlds.rectangleBorderSet(
                        centerX = border.centerX,
                        centerZ = border.centerZ,
                        width = border.width,
                        depth = border.depth,
                    )
                }
                is WorldBorder.Square -> {
                    l10n.worlds.borderSet(
                        shape = border.shape.name.lowercase(),
                        centerX = border.centerX,
                        centerZ = border.centerZ,
                        radius = border.radius,
                    )
                }
                is WorldBorder.Circle -> {
                    l10n.worlds.borderSet(
                        shape = border.shape.name.lowercase(),
                        centerX = border.centerX,
                        centerZ = border.centerZ,
                        radius = border.radius,
                    )
                }
            }
        player.sendMessage(message)
        if (relocationFailures.isNotEmpty()) {
            player.sendMessage(l10n.worlds.borderRelocationFailures(relocationFailures.size))
        }
    }
}
