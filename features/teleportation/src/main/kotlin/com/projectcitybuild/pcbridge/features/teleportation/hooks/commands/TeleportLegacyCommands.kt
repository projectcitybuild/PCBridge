package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.withCoordinateArgs
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.Server
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
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission("essentials.tpa")
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    val player = context.source.requirePlayer()
                    val target = Argument.onlinePlayer("player", server).get(context)
                    teleportService.to(player, target)
                }
            }
        }
    }

    private fun tpahere() = command("tpahere") {
        description = "Requests that a player teleport to you"
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission("essentials.tpahere")
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    val player = context.source.requirePlayer()
                    val target = Argument.onlinePlayer("player", server).get(context)
                    teleportService.requestHere(player, target)
                }
            }
        }
    }

    private fun tpo() = command("tpo") {
        description = "Teleports you to a player, bypassing teleport requests"
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission("essentials.tpo")
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    val target = Argument.onlinePlayer("player", server).get(context)
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
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission("essentials.tpohere")
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.here(
                        operator = context.source.requirePlayer(),
                        target = Argument.onlinePlayer("player", server).get(context),
                        flags = TeleportFlags(force = true),
                    )
                }
            }
        }
    }

    private fun tphere() = command("tphere") {
        description = "Teleports a player to you"
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission("essentials.tphere")
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.here(
                        operator = context.source.requirePlayer(),
                        target = Argument.onlinePlayer("player", server).get(context),
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
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.cancel(context.source.requirePlayer(), targetId = null)
                }
            }
            argument(Argument.onlinePlayer("player", server)) {
                requiresPermission("essentials.tpcancel")
                executesSuspending(plugin) { context ->
                    context.runCommandTracedSafely(teleportationTracer) {
                        teleportService.cancel(
                            context.source.requirePlayer(),
                            Argument.onlinePlayer("player", server).get(context).uniqueId,
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
            context.runCommandTracedSafely(teleportationTracer) {
                val player = context.source.requirePlayer()
                teleportService.back(player, player)
            }
        }
        argument(Argument.onlinePlayer("player", server)) {
            requiresPermission(TeleportationPermissions.BACK_OTHERS)
            executesSuspending(plugin) { context ->
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.back(
                        operator = context.source.sender,
                        target = Argument.onlinePlayer("player", server).get(context),
                    )
                }
            }
        }
    }

    private fun rtp() = command("rtp") {
        description = "Teleports you to a random location"
        requiresPermission(TeleportationPermissions.RANDOM)
        executesSuspending(plugin) { context ->
            context.runCommandTracedSafely(teleportationTracer) {
                teleportService.random(context.source.requirePlayer())
            }
        }
    }

    private fun tppos() = command("tppos") {
        description = "Teleports you to coordinates"
        requiresPermission(TeleportationPermissions.POSITION)
        withCoordinateArgs(plugin) { context, x, y, z, yaw, pitch ->
            teleportService.position(
                player = context.source.requirePlayer(),
                x = x,
                y = y,
                z = z,
                yaw = yaw,
                pitch = pitch,
            )
        }
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
        argument(Argument.onlinePlayer("target", server)) {
            executesSuspending(plugin) { context ->
                teleportService.all(
                    operator = context.source.sender,
                    requestedTarget = Argument.onlinePlayer("target", server).get(context),
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
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.accept(context.source.requirePlayer(), requesterId = null)
                }
            }
            argument(Argument.onlinePlayer("requester", server)) {
                requiresPermission("essentials.tpaccept")
                executesSuspending(plugin) { context ->
                    context.runCommandTracedSafely(teleportationTracer) {
                        teleportService.accept(
                            context.source.requirePlayer(),
                            Argument.onlinePlayer("requester", server).get(context).uniqueId,
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
                context.runCommandTracedSafely(teleportationTracer) {
                    teleportService.deny(context.source.requirePlayer(), requesterId = null)
                }
            }
            argument(Argument.onlinePlayer("requester", server)) {
                executesSuspending(plugin) { context ->
                    context.runCommandTracedSafely(teleportationTracer) {
                        teleportService.deny(
                            context.source.requirePlayer(),
                            Argument.onlinePlayer("requester", server).get(context).uniqueId,
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
        override val builder = commandDsl(name, configure)
    }
}
