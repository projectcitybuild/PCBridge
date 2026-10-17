package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.SingleOnlinePlayerArgument
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
    fun commands(): List<BrigadierCommand> =
        buildList {
            addAll(
                playerCommands(
                    names = listOf("tpa"),
                    permission = PermissionNode.TELEPORT_REQUEST,
                ) { context, target ->
                    application.to(context.source.requirePlayer(), target)
                },
            )
            addAll(
                playerCommands(
                    names = listOf("tpahere"),
                    permission = PermissionNode.TELEPORT_REQUEST,
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
                    permission = PermissionNode.TELEPORT_REQUEST,
                    additionalPermissions = listOf(PermissionNode.TELEPORT_FORCE),
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
                    permission = PermissionNode.TELEPORT_OTHERS,
                    additionalPermissions = listOf(PermissionNode.TELEPORT_FORCE),
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
                    permission = PermissionNode.TELEPORT_OTHERS,
                ) { context, target ->
                    application.here(context.source.requirePlayer(), target)
                },
            )
            addAll(
                allCommands(listOf("tpall")),
            )
            addAll(
                positionCommands(listOf("tppos")),
            )
            addAll(
                backCommands(listOf("back")),
            )
            add(legacyAlias())
        }

    private fun legacyAlias(): BrigadierCommand = randomCommands(listOf("rtp")).single()

    private fun playerCommands(
        names: List<String>,
        permission: PermissionNode,
        additionalPermissions: List<PermissionNode> = emptyList(),
        action: suspend (PaperCommandContext, Player) -> Unit,
    ): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                then(
                    Commands.argument("player", SingleOnlinePlayerArgument(server))
                        .requiresPermission(permission, *additionalPermissions.toTypedArray())
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
    ): List<BrigadierCommand> =
        names.map { name ->
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
                    Commands.argument("requester", SingleOnlinePlayerArgument(server))
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

    private fun cancelCommands(names: List<String>): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.cancel(context.source.requirePlayer(), targetId = null)
                    }
                }
                then(
                    Commands.argument("player", SingleOnlinePlayerArgument(server))
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

    private fun backCommands(names: List<String>): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        val player = context.source.requirePlayer()
                        application.back(player, player)
                    }
                }
                then(
                    Commands.argument("player", SingleOnlinePlayerArgument(server))
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
        }

    private fun randomCommands(names: List<String>): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                requiresPermission(PermissionNode.TELEPORT_RANDOM)
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        application.random(context.source.requirePlayer())
                    }
                }
            }
        }

    private fun positionCommands(names: List<String>): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                requiresPermission(PermissionNode.TELEPORT_POSITION)
                then(
                    Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(
                            Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(
                                    Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executesSuspending(plugin) { executePosition(it, null) }
                                        .then(
                                            Commands.argument("world", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    server.worlds.forEach { builder.suggest(it.name) }
                                                    builder.buildFuture()
                                                }
                                                .executesSuspending(plugin) { executePosition(it, "world") },
                                        ),
                                ),
                        ),
                )
            }
        }

    private fun allCommands(names: List<String>): List<BrigadierCommand> =
        names.map { name ->
            command(name) {
                requiresPermission(PermissionNode.TELEPORT_ALL)
                executesSuspending(plugin) { context ->
                    application.all(
                        operator = context.source.sender,
                        requestedTarget = null,
                        flags = TeleportFlags(force = true),
                    )
                }
                then(
                    Commands.argument("target", SingleOnlinePlayerArgument(server))
                        .executesSuspending(plugin) { context ->
                            application.all(
                                operator = context.source.sender,
                                requestedTarget = context.getArgument("target", Player::class.java),
                                flags = TeleportFlags(force = true),
                            )
                        }
                )
            }
        }

    private suspend fun executePosition(
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

    private fun command(
        name: String,
        configure: com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>.() -> Unit,
    ): BrigadierCommand =
        object : BrigadierCommand {
            override fun literal(): PaperCommandNode =
                Commands.literal(name)
                    .apply(configure)
                    .build()
        }
}
