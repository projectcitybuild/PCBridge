package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpMoveCommand(
    private val warpRepository: WarpRepository,
) : SpigotCommand<WarpMoveCommand.Args> {
    override val label = "move"

    override val usage = CommandHelpBuilder(usage = "/warps move <name>")

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
        warpRepository.move(
            name = args.warpName,
            world = player.location.world.name,
            x = player.location.x,
            y = player.location.y,
            z = player.location.z,
            pitch = player.location.pitch,
            yaw = player.location.yaw,
        )
        sender.sendMessage(
            Component.text("${args.warpName} warp moved")
                .color(NamedTextColor.GREEN),
        )
    }

    data class Args(
        val warpName: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.size != 1) {
                    throw BadCommandUsageException()
                }
                return Args(warpName = args[0])
            }
        }
    }
}
