package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import org.bukkit.plugin.Plugin

class BorderSetCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("set") {
        description = l10n.worlds.borderSetCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        literal("square") {
            radiusArguments(WorldBorderShape.SQUARE)
        }
        literal("circle") {
            radiusArguments(WorldBorderShape.CIRCLE)
        }
        literal("rectangle") {
            rectangleArguments()
        }
    }

    private fun CommandBuilder.radiusArguments(shape: WorldBorderShape) {
        argument("radius", DoubleArgumentType.doubleArg(1.0, SerializableWorldBorder.MAX_RADIUS)) {
            executesSuspending(taskExecutor) { context ->
                set(
                    context = context,
                    shape = shape,
                    radius = DoubleArgumentType.getDouble(context, "radius"),
                )
            }
            argument("x", DoubleArgumentType.doubleArg()) {
                argument("z", DoubleArgumentType.doubleArg()) {
                    executesSuspending(taskExecutor) { context ->
                        set(
                            context = context,
                            shape = shape,
                            radius = DoubleArgumentType.getDouble(context, "radius"),
                            centerX = DoubleArgumentType.getDouble(context, "x"),
                            centerZ = DoubleArgumentType.getDouble(context, "z"),
                        )
                    }
                }
            }
        }
    }

    private fun CommandBuilder.rectangleArguments() {
        argument("width", DoubleArgumentType.doubleArg(1.0, SerializableWorldBorder.MAX_DIMENSION)) {
            argument("depth", DoubleArgumentType.doubleArg(1.0, SerializableWorldBorder.MAX_DIMENSION)) {
                executesSuspending(taskExecutor) { context ->
                    set(
                        context = context,
                        shape = WorldBorderShape.RECTANGLE,
                        width = DoubleArgumentType.getDouble(context, "width"),
                        depth = DoubleArgumentType.getDouble(context, "depth"),
                    )
                }
                argument("x", DoubleArgumentType.doubleArg()) {
                    argument("z", DoubleArgumentType.doubleArg()) {
                        executesSuspending(taskExecutor) { context ->
                            set(
                                context = context,
                                shape = WorldBorderShape.RECTANGLE,
                                width = DoubleArgumentType.getDouble(context, "width"),
                                depth = DoubleArgumentType.getDouble(context, "depth"),
                                centerX = DoubleArgumentType.getDouble(context, "x"),
                                centerZ = DoubleArgumentType.getDouble(context, "z"),
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun set(
        context: PaperCommandContext,
        shape: WorldBorderShape,
        radius: Double? = null,
        width: Double? = null,
        depth: Double? = null,
        centerX: Double? = null,
        centerZ: Double? = null,
    ) {
        val player = context.source.requirePlayer()
        val border = try {
            SerializableWorldBorder(
                centerX = centerX ?: player.location.x,
                centerZ = centerZ ?: player.location.z,
                shape = shape,
                radius = radius,
                width = width,
                depth = depth,
            ).validate()
        } catch (failure: IllegalArgumentException) {
            player.sendMessage(l10n.worlds.invalidBorder(failure.message))
            return
        }

        worldBorderRepository.set(player.world, border)

        eventBroadcaster.broadcast(
            WorldBorderUpdatedEvent(player.world, border),
        )
        player.sendMessage(
            if (shape == WorldBorderShape.RECTANGLE) {
                l10n.worlds.rectangleBorderSet(
                    centerX = border.centerX,
                    centerZ = border.centerZ,
                    width = requireNotNull(border.width),
                    depth = requireNotNull(border.depth),
                )
            } else {
                l10n.worlds.borderSet(
                    shape = shape.name.lowercase(),
                    centerX = border.centerX,
                    centerZ = border.centerZ,
                    radius = requireNotNull(border.radius),
                )
            },
        )
    }
}
