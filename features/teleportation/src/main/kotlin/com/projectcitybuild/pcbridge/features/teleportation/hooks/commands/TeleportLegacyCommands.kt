package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportLegacyCommands(
    private val plugin: Plugin,
    private val server: Server,
    private val application: TeleportApplication,
) {
    fun commands(): List<BrigadierCommand> = buildList {
        addAll(
            playerCommands(
                names = listOf("tpa"),
                permissions = listOf(PermissionNode.TELEPORT_REQUEST),
            ) { context, target ->
                application.to(context.source.requirePlayer(), target)
            },
        )
        addAll(
            playerCommands(
                names = listOf("tpahere"),
                permissions = listOf(PermissionNode.TELEPORT_REQUEST),
            ) { context, target ->
                application.requestHere(context.source.requirePlayer(), target)
            },
        )
        addAll(
            requestResponseCommands(
                names = listOf("tpaccept", "tpyes"),
                accept = true,
            ),
        )
        addAll(
            requestResponseCommands(
                names = listOf("tpdeny", "tpno"),
                accept = false,
            ),
        )
        addAll(
            cancelCommands(listOf("tpcancel", "tpacancel")),
        )
        addAll(
            playerCommands(
                names = listOf("tpo"),
                permissions = listOf(PermissionNode.TELEPORT_REQUEST, PermissionNode.TELEPORT_FORCE),
            ) { context, target ->
                application.to(
                    requester = context.source.requirePlayer(),
                    target = target,
                    flags = TeleportFlags(force = true),
                )
            },
        )
        addAll(
            playerCommands(
                names = listOf("tpohere"),
                permissions = listOf(PermissionNode.TELEPORT_OTHERS, PermissionNode.TELEPORT_FORCE),
            ) { context, target ->
                application.here(
                    operator = context.source.requirePlayer(),
                    target = target,
                    flags = TeleportFlags(force = true),
                )
            },
        )
        addAll(
            playerCommands(
                names = listOf("tphere"),
                permissions = listOf(PermissionNode.TELEPORT_OTHERS, PermissionNode.TELEPORT_FORCE),
            ) { context, target ->
                application.here(
                    operator = context.source.requirePlayer(),
                    target = target,
                    flags = TeleportFlags(force = true),
                )
            },
        )
        addAll(
            allCommands(listOf("tpall")),
        )
        addAll(
            positionCommands(listOf("tppos")),
        )
        add(backCommand())
        add(randomCommand())
    }

    private fun playerCommands(
        names: List<String>,
        permissions: List<PermissionNode>,
        action: suspend (PaperCommandContext, Player) -> Unit,
    ): List<BrigadierCommand> = names.map { name ->
        command(name) {
            then(
                Commands
                    .argument("player", SingleOnlinePlayerArgument(server))
                    .requiresPermission(*permissions.toTypedArray())
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            action(context, context.getArgument("player", Player::class.java))
                        }
                    },
            )
        }
    }

    private fun requestResponseCommands(
        names: List<String>,
        accept: Boolean,
    ): List<BrigadierCommand> = names.map { name ->
        command(name) {
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    if (accept) {
                        application.accept(context.source.requirePlayer(), requesterId = null)
                    } else {
                        application.deny(context.source.requirePlayer(), requesterId = null)
                    }
                }
            }
            then(
                Commands
                    .argument("requester", SingleOnlinePlayerArgument(server))
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            val requester = context.getArgument("requester", Player::class.java)
                            if (accept) {
                                application.accept(context.source.requirePlayer(), requester.uniqueId)
                            } else {
                                application.deny(context.source.requirePlayer(), requester.uniqueId)
                            }
                        }
                    },
            )
        }
    }

    private fun cancelCommands(names: List<String>): List<BrigadierCommand> = names.map { name ->
        command(name) {
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    application.cancel(context.source.requirePlayer(), targetId = null)
                }
            }
            then(
                Commands
                    .argument("player", SingleOnlinePlayerArgument(server))
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            application.cancel(
                                context.source.requirePlayer(),
                                context.getArgument("player", Player::class.java).uniqueId,
                            )
                        }
                    },
            )
        }
    }

    private fun backCommand(): BrigadierCommand = command("back") {
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                val player = context.source.requirePlayer()
                application.back(player, player)
            }
        }
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission(PermissionNode.TELEPORT_BACK_OTHERS)
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.back(
                            operator = context.source.sender,
                            target = context.getArgument("player", Player::class.java),
                        )
                    }
                },
        )
    }

    private fun randomCommand(): BrigadierCommand = command("rtp") {
        requiresPermission(PermissionNode.TELEPORT_RANDOM)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                application.random(context.source.requirePlayer())
            }
        }
    }

    private fun positionCommands(names: List<String>): List<BrigadierCommand> = names.map { name ->
        command(name) {
            requiresPermission(PermissionNode.TELEPORT_POSITION)
            then(
                withCoordinateArgs(plugin, ::executePosition),
            )
        }
    }

    private fun allCommands(names: List<String>): List<BrigadierCommand> = names.map { name ->
        command(name) {
            requiresPermission(PermissionNode.TELEPORT_ALL, PermissionNode.TELEPORT_FORCE)
            executesSuspending(plugin) { context ->
                application.all(
                    operator = context.source.sender,
                    requestedTarget = null,
                    flags = TeleportFlags(force = true),
                )
            }
            then(
                Commands
                    .argument("target", SingleOnlinePlayerArgument(server))
                    .executesSuspending(plugin) { context ->
                        application.all(
                            operator = context.source.sender,
                            requestedTarget = context.getArgument("target", Player::class.java),
                            flags = TeleportFlags(force = true),
                        )
                    },
            )
        }
    }

    private suspend fun executePosition(
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

    private fun command(
        name: String,
        configure: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit,
    ): BrigadierCommand = object : BrigadierCommand {
        override fun literal(): PaperCommandNode = Commands
            .literal(name)
            .apply(configure)
            .build()
    }
}
