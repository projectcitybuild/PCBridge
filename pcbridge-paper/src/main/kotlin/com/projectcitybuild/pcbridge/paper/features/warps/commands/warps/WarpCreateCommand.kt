package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.projectcitybuild.pcbridge.paper.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.models.Warp
import com.projectcitybuild.pcbridge.paper.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpCreateCommand(
    private val warpRepository: WarpRepository,
    private val server: Server,
    private val time: LocalizedTime,
) : SpigotCommand<WarpCreateCommand.Args> {
    override val label = "create"

    override val usage = CommandHelpBuilder(usage = "/warps create <name> [x:] [y:] [z:] [pitch:] [yaw:] [world:]")

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        if (!sender.hasPermission("pcbridge.warp.manage")) {
            throw UnauthorizedCommandException()
        }
        val player = sender as? Player
        checkNotNull(player) {
            "Only players can use this command"
        }

        val world =
            if (args.worldName.isNullOrEmpty()) {
                player.location.world
            } else {
                server.getWorld(args.worldName)
            }
        checkNotNull(world) {
            "World ${args.worldName} not found"
        }

        val warp =
            Warp(
                id = -1,
                name = args.warpName,
                world = world.name,
                x = args.x ?: player.location.x,
                y = args.y ?: player.location.y,
                z = args.z ?: player.location.z,
                yaw = args.yaw ?: player.location.yaw,
                pitch = args.pitch ?: player.location.pitch,
            )
        warpRepository.create(warp)

        server.pluginManager.callEvent(WarpCreateEvent())

        sender.sendMessage(
            Component.text("${args.warpName} warp created")
                .color(NamedTextColor.GREEN),
        )
    }

    data class Args(
        val warpName: String,
        val worldName: String?,
        val x: Double?,
        val y: Double?,
        val z: Double?,
        val yaw: Float?,
        val pitch: Float?,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                val warpName = args[0]
                val remainingArgs = args.drop(1)

                return Args(
                    warpName = warpName,
                    worldName = remainingArgs
                        .find { it.startsWith("world:") }
                        ?.removePrefix("world:"),
                    x = remainingArgs
                        .find { it.startsWith("x:") }
                        ?.removePrefix("x:")
                        ?.toDoubleOrNull(),
                    y = remainingArgs
                        .find { it.startsWith("y:") }
                        ?.removePrefix("y:")
                        ?.toDoubleOrNull(),
                    z = remainingArgs
                        .find { it.startsWith("z:") }
                        ?.removePrefix("z:")
                        ?.toDoubleOrNull(),
                    yaw = remainingArgs
                        .find { it.startsWith("yaw:") }
                        ?.removePrefix("yaw:")
                        ?.toFloatOrNull(),
                    pitch = remainingArgs
                        .find { it.startsWith("pitch:") }
                        ?.removePrefix("pitch:")
                        ?.toFloatOrNull(),
                )
            }
        }
    }
}
