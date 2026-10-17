package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class TeleportPosCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("pos")
            .requiresPermission(PermissionNode.TELEPORT_POSITION)
            .then(
                Commands.argument("x", DoubleArgumentType.doubleArg())
                    .then(
                        Commands.argument("y", DoubleArgumentType.doubleArg())
                            .then(
                                Commands.argument("z", DoubleArgumentType.doubleArg())
                                    .executesSuspending(plugin) { execute(it, null) }
                                    .then(
                                        Commands.argument("world", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                server.worlds.forEach { builder.suggest(it.name) }
                                                builder.buildFuture()
                                            }
                                            .executesSuspending(plugin) { execute(it, "world") },
                                    ),
                            ),
                    ),
            )
            .build()

    private suspend fun execute(
        context: PaperCommandContext,
        worldArgument: String?,
    ) = context.scoped(teleportationTracer) {
        application.position(
            player = context.source.requirePlayer(),
            x = DoubleArgumentType.getDouble(context, "x"),
            y = DoubleArgumentType.getDouble(context, "y"),
            z = DoubleArgumentType.getDouble(context, "z"),
            worldName = worldArgument?.let { context.getArgument(it, String::class.java) },
        )
    }
}
