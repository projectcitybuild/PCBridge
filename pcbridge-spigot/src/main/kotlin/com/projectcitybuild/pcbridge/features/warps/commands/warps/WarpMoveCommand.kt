package com.projectcitybuild.pcbridge.features.warps.commands.warps

import com.projectcitybuild.pcbridge.data.SerializableLocation
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
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
            newLocation = SerializableLocation.fromLocation(player.location),
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
