package com.projectcitybuild.pcbridge.features.warps.commands.warps

import com.projectcitybuild.pcbridge.data.SerializableLocation
import com.projectcitybuild.pcbridge.features.warps.Warp
import com.projectcitybuild.pcbridge.features.warps.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.LocalDateTime

class WarpCreateCommand(
    private val warpRepository: WarpRepository,
    private val server: Server,
): SpigotCommand<WarpCreateCommand.Args> {
    override val label = "create"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        if (!sender.hasPermission("pcbridge.warp.manage")) {
            throw UnauthorizedCommandException()
        }
        val player = sender as? Player
        checkNotNull (player) {
            "Only players can use this command"
        }

        val world = if (args.worldName.isNullOrEmpty()) {
            player.location.world
        } else {
            server.getWorld(args.worldName)
        }
        checkNotNull(world) {
            "World ${args.worldName} not found"
        }

        val location = SerializableLocation(
            worldName = world.name,
            x = args.x ?: player.location.x,
            y = args.y ?: player.location.y,
            z = args.z ?: player.location.z,
            yaw = args.yaw ?: player.location.yaw,
            pitch = args.pitch ?: player.location.pitch,
        )
        val warp = Warp(
            name = args.warpName,
            location = location,
            createdAt = LocalDateTime.now(), // TODO
        )
        warpRepository.create(warp)

        server.pluginManager.callEvent(WarpCreateEvent())

        sender.sendMessage(
            Component.text("${args.warpName} warp created")
                .color(NamedTextColor.GREEN)
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
        class Parser: CommandArgsParser<Args> {
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
                    x = "x:".let { prefix ->
                        remainingArgs
                            .find { it.startsWith(prefix) }
                            ?.removePrefix(prefix)
                            ?.toDoubleOrNull()
                    },
                    y = "y:".let { prefix ->
                        remainingArgs
                            .find { it.startsWith(prefix) }
                            ?.removePrefix(prefix)
                            ?.toDoubleOrNull()
                    },
                    z = "z:".let { prefix ->
                        remainingArgs
                            .find { it.startsWith(prefix) }
                            ?.removePrefix(prefix)
                            ?.toDoubleOrNull()
                    },
                    yaw = "yaw:".let { prefix ->
                        remainingArgs
                            .find { it.startsWith(prefix) }
                            ?.removePrefix(prefix)
                            ?.toFloatOrNull()
                    },
                    pitch = "pitch:".let { prefix ->
                        remainingArgs
                            .find { it.startsWith(prefix) }
                            ?.removePrefix(prefix)
                            ?.toFloatOrNull()
                    },
                )
            }
        }
    }
}