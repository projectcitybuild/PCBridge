package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import com.projectcitybuild.pcbridge.platform.paper.commands.command as commandDsl

class TeleportLegacyCommands(
    private val plugin: Plugin,
    private val server: Server,
    private val teleportService: TeleportService,
) {
    fun commands() = buildList {
        add(tphere())
        add(tpa())
        add(tpahere())
        addAll(tpaccept())
        addAll(tpdeny())
        addAll(tpcancel())
        add(tpo())
        add(tpohere())
        add(tpall())
        add(tppos())
        add(back())
        add(rtp())
    }

    // TODO: support tptoggle
    private fun tpa() = command("tpa") {
        description = "Sends a teleport request to a player"
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission("essentials.tpa")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    val player = context.source.requirePlayer()
                    val target = context.getArgument("player", Player::class.java)
                    teleportService.to(player, target)
                }
            }
        }
    }

    private fun tpahere() = command("tpahere") {
        description = "Requests that a player teleport to you"
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission("essentials.tpahere")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    val player = context.source.requirePlayer()
                    val target = context.getArgument("player", Player::class.java)
                    teleportService.requestHere(player, target)
                }
            }
        }
    }

    private fun tpo() = command("tpo") {
        description = "Teleports you to a player, bypassing teleport requests"
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission("essentials.tpo")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    val target = context.getArgument("player", Player::class.java)
                    teleportService.to(
                        requester = context.source.requirePlayer(),
                        target = target,
                        flags = TeleportFlags(force = true),
                    )
                }
            }
        }
    }

    private fun tpohere() = command("tpohere") {
        description = "Teleports a player to you, bypassing teleport requests"
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission("essentials.tpohere")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.here(
                        operator = context.source.requirePlayer(),
                        target = context.getArgument("player", Player::class.java),
                        flags = TeleportFlags(force = true),
                    )
                }
            }
        }
    }

    private fun tphere() = command("tphere") {
        description = "Teleports a player to you"
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission("essentials.tphere")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.here(
                        operator = context.source.requirePlayer(),
                        target = context.getArgument("player", Player::class.java),
                        flags = TeleportFlags(force = true),
                    )
                }
            }
        }
    }

    private fun tpcancel() = listOf("tpcancel", "tpacancel").map {
        command(it) {
            description = "Cancels a pending teleport request"
            requiresPermission("essentials.tpcancel")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.cancel(context.source.requirePlayer(), targetId = null)
                }
            }
            argument("player", SingleOnlinePlayerArgument(server)) {
                requiresPermission("essentials.tpcancel")
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.cancel(
                            context.source.requirePlayer(),
                            context.getArgument("player", Player::class.java).uniqueId,
                        )
                    }
                }
            }
        }
    }

    private fun back() = command("back") {
        description = "Returns you to your previous location"
        requiresPermission(TeleportationPermissions.BACK)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                val player = context.source.requirePlayer()
                teleportService.back(player, player)
            }
        }
        argument("player", SingleOnlinePlayerArgument(server)) {
            requiresPermission(TeleportationPermissions.BACK_OTHERS)
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.back(
                        operator = context.source.sender,
                        target = context.getArgument("player", Player::class.java),
                    )
                }
            }
        }
    }

    private fun rtp() = command("rtp") {
        description = "Teleports you to a random location"
        requiresPermission(TeleportationPermissions.RANDOM)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                teleportService.random(context.source.requirePlayer())
            }
        }
    }

    private fun tppos() = command("tppos") {
        description = "Teleports you to coordinates"
        requiresPermission(TeleportationPermissions.POSITION)
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

    private fun tpall() = command("tpall") {
        description = "Teleports all players to a target"
        requiresPermission(TeleportationPermissions.ALL, TeleportationPermissions.FORCE)
        executesSuspending(plugin) { context ->
            teleportService.all(
                operator = context.source.sender,
                requestedTarget = null,
                flags = TeleportFlags(force = true),
            )
        }
        argument("target", SingleOnlinePlayerArgument(server)) {
            executesSuspending(plugin) { context ->
                teleportService.all(
                    operator = context.source.sender,
                    requestedTarget = context.getArgument("target", Player::class.java),
                    flags = TeleportFlags(force = true),
                )
            }
        }
    }

    private fun tpaccept() = listOf("tpaccept", "tpyes").map { name ->
        command(name) {
            description = "Accepts a pending teleport request"
            requiresPermission("essentials.tpaccept")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.accept(context.source.requirePlayer(), requesterId = null)
                }
            }
            argument("requester", SingleOnlinePlayerArgument(server)) {
                requiresPermission("essentials.tpaccept")
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.accept(
                            context.source.requirePlayer(),
                            context.getArgument("requester", Player::class.java).uniqueId,
                        )
                    }
                }
            }
        }
    }

    private fun tpdeny() = listOf("tpdeny", "tpno").map { name ->
        command(name) {
            description = "Declines a pending teleport request"
            requiresPermission("essentials.tpdeny")
            executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    teleportService.deny(context.source.requirePlayer(), requesterId = null)
                }
            }
            argument("requester", SingleOnlinePlayerArgument(server)) {
                executesSuspending(plugin) { context ->
                    context.scoped(teleportationTracer) {
                        teleportService.deny(
                            context.source.requirePlayer(),
                            context.getArgument("requester", Player::class.java).uniqueId,
                        )
                    }
                }
            }
        }
    }

    private fun command(
        name: String,
        configure: CommandBuilder.() -> Unit,
    ): BrigadierCommand = object : BrigadierCommand {
        override val commandDefinition by lazy { commandDsl(name, configure) }
    }
        }
