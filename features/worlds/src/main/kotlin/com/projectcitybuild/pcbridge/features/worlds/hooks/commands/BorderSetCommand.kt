package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin

class BorderSetCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder =
        command("set") {
            description = "Sets a square border; use circle for a circular border"
            requiresPermission(WorldsPermissions.MANAGE)
            then(radiusArgument(WorldBorderShape.SQUARE))
            literal("circle") {
                then(radiusArgument(WorldBorderShape.CIRCLE))
            }
        }

    private fun radiusArgument(shape: WorldBorderShape): RequiredArgumentBuilder<CommandSourceStack, Double> = RequiredArgumentBuilder
        .argument<CommandSourceStack, Double>(
            "radius",
            DoubleArgumentType.doubleArg(1.0, SerializableWorldBorder.MAX_RADIUS),
        ).executesSuspending(taskExecutor) { context ->
            set(context, shape, null, null)
        }.then(
            RequiredArgumentBuilder
                .argument<CommandSourceStack, Double>("x", DoubleArgumentType.doubleArg())
                .then(
                    RequiredArgumentBuilder
                        .argument<CommandSourceStack, Double>("z", DoubleArgumentType.doubleArg())
                        .executesSuspending(taskExecutor) { context ->
                            set(
                                context,
                                shape,
                                DoubleArgumentType.getDouble(context, "x"),
                                DoubleArgumentType.getDouble(context, "z"),
                            )
                        },
                ),
        )

    private suspend fun set(
        context: PaperCommandContext,
        shape: WorldBorderShape,
        centerX: Double?,
        centerZ: Double?,
    ) {
        val player = context.source.requirePlayer()
        val radius = DoubleArgumentType.getDouble(context, "radius")
        val border =
            try {
                SerializableWorldBorder(
                    centerX = centerX ?: player.location.x,
                    centerZ = centerZ ?: player.location.z,
                    radius = radius,
                    shape = shape,
                ).validate()
            } catch (failure: IllegalArgumentException) {
                player.sendMessage("Invalid world border: ${failure.message}")
                return
            }

        worldBorderRepository.set(player.world, border)
        eventBroadcaster.broadcast(WorldBorderUpdatedEvent(player.world, border))
        val shapeDescription = if (shape == WorldBorderShape.CIRCLE) "circular" else "square"
        player.sendMessage(
            "World border set: $shapeDescription, center ${border.centerX}, ${border.centerZ}; " +
                "radius ${border.radius} blocks.",
        )
    }
}
