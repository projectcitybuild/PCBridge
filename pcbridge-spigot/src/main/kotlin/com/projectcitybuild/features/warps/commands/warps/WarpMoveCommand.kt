package com.projectcitybuild.features.warps.commands.warps

import com.projectcitybuild.data.SerializableLocation
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpMoveCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
): SpigotCommand<WarpMoveCommand.Args> {
    override val label = "move"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        val player = sender as? Player
        checkNotNull (player) {
            "Only players can use this command"
        }
        warpRepository.move(
            name = args.warpName,
            newLocation = SerializableLocation.fromLocation(player.location),
        )

        val message = Component.text("${args.warpName} warp was moved")
            .color(NamedTextColor.GREEN)

        audiences.sender(sender).sendMessage(message)
    }

    data class Args(
        val warpName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun tryParse(args: List<String>): Args {
                if (args.size != 1) {
                    throw BadCommandUsageException()
                }
                return Args(warpName = args[0])
            }
        }
    }
}