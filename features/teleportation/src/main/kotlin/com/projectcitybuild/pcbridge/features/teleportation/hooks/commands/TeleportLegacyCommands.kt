package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
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
    private val teleportService: TeleportService,
) {
    fun commands(): List<BrigadierCommand> = buildList {
        add(tp())
        add(tpahere())
        addAll(tpaccept())
        addAll(tpdeny())
        addAll(tpcancel())
        add(tpo())
        add(tpohere())
        add(tphere())
        add(tpall())
        add(tppos())
        add(back())
        add(rtp())
    }

    private fun tp() = command("tp") {
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission("essentials.tp")
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        val player = context.source.requirePlayer()
                        val target = context.getArgument("player", Player::class.java)
                        teleportService.to(player, target)
                    }
                },
        )
    }

    private fun tpahere() = command("tpahere") {
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission("essentials.tpahere")
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        val player = context.source.requirePlayer()
                        val target = context.getArgument("player", Player::class.java)
                        teleportService.requestHere(player, target)
                    }
                },
        )
    }

    private fun tpo() = command("tpo") {
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission("essentials.tpo")
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        val target = context.getArgument("player", Player::class.java)
                        teleportService.to(
                            requester = context.source.requirePlayer(),
                            target = target,
                            flags = TeleportFlags(force = true),
                        )
                    }
                },
        )
    }

    private fun tpohere() = command("tpohere") {
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission("essentials.tpohere")
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.here(
                            operator = context.source.requirePlayer(),
                            target = context.getArgument("player", Player::class.java),
                            flags = TeleportFlags(force = true),
                        )
                    }
                },
        )
    }

    private fun tphere() = command("tphere") {
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission("essentials.tphere")
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.here(
                            operator = context.source.requirePlayer(),
                            target = context.getArgument("player", Player::class.java),
                        )
                    }
                },
        )
    }

    private fun tpcancel() = listOf("tpcancel", "tpacancel").map {
        command(it) {
            requiresPermission("essentials.tpcancel")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.cancel(context.source.requirePlayer(), targetId = null)
                }
            }
            then(
                Commands
                    .argument("player", SingleOnlinePlayerArgument(server))
                    .requiresPermission("essentials.tpcancel")
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            teleportService.cancel(
                                context.source.requirePlayer(),
                                context.getArgument("player", Player::class.java).uniqueId,
                            )
                        }
                    },
            )
        }
    }

    private fun back(): BrigadierCommand = command("back") {
        requiresPermission(PermissionNode.TELEPORT_BACK)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                val player = context.source.requirePlayer()
                teleportService.back(player, player)
            }
        }
        then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(server))
                .requiresPermission(PermissionNode.TELEPORT_BACK_OTHERS)
                .executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.back(
                            operator = context.source.sender,
                            target = context.getArgument("player", Player::class.java),
                        )
                    }
                },
        )
    }

    private fun rtp(): BrigadierCommand = command("rtp") {
        requiresPermission(PermissionNode.TELEPORT_RANDOM)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                teleportService.random(context.source.requirePlayer())
            }
        }
    }

    private fun tppos(): BrigadierCommand = command("tppos") {
        requiresPermission(PermissionNode.TELEPORT_POSITION)
        then(
            withCoordinateArgs(plugin) { context, yaw, pitch ->
                teleportService.position(
                    player = context.source.requirePlayer(),
                    x = DoubleArgumentType.getDouble(context, "x"),
                    y = DoubleArgumentType.getDouble(context, "y"),
                    z = DoubleArgumentType.getDouble(context, "z"),
                    yaw = yaw,
                    pitch = pitch,
                )
            },
        )
    }

    private fun tpall(): BrigadierCommand = command("tpall") {
        requiresPermission(PermissionNode.TELEPORT_ALL, PermissionNode.TELEPORT_FORCE)
        executesSuspending(plugin) { context ->
            teleportService.all(
                operator = context.source.sender,
                requestedTarget = null,
                flags = TeleportFlags(force = true),
            )
        }
        then(
            Commands
                .argument("target", SingleOnlinePlayerArgument(server))
                .executesSuspending(plugin) { context ->
                    teleportService.all(
                        operator = context.source.sender,
                        requestedTarget = context.getArgument("target", Player::class.java),
                        flags = TeleportFlags(force = true),
                    )
                },
        )
    }

    private fun tpaccept() = listOf("tpaccept", "tpyes").map { name ->
        command(name) {
            requiresPermission("essentials.tpaccept")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.accept(context.source.requirePlayer(), requesterId = null)
                }
            }
            then(
                Commands
                    .argument("requester", SingleOnlinePlayerArgument(server))
                    .requiresPermission("essentials.tpaccept")
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            teleportService.accept(
                                context.source.requirePlayer(),
                                context.getArgument("requester", Player::class.java).uniqueId,
                            )
                        }
                    },
            )
        }
    }

    private fun tpdeny() = listOf("tpdeny", "tpno").map { name ->
        command(name) {
            requiresPermission("essentials.tpdeny")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.deny(context.source.requirePlayer(), requesterId = null)
                }
            }
            then(
                Commands
                    .argument("requester", SingleOnlinePlayerArgument(server))
                    .executesSuspending(plugin) { context ->
                        context.scoped(teleportationTracer) {
                            teleportService.deny(
                                context.source.requirePlayer(),
                                context.getArgument("requester", Player::class.java).uniqueId,
                            )
                        }
                    },
            )
        }
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
