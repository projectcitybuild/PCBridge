package com.projectcitybuild.features.warps.commands.warps

import com.projectcitybuild.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import com.projectcitybuild.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.command.CommandSender

class WarpDeleteCommand(
    private val warpRepository: WarpRepository,
    private val server: Server,
): SpigotCommand<WarpDeleteCommand.Args> {
    override val label = "delete"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        if (!sender.hasPermission("pcbridge.warp.manage")) {
            throw UnauthorizedCommandException()
        }
        warpRepository.delete(name = args.warpName)

        server.pluginManager.callEvent(WarpDeleteEvent())

        sender.sendMessage(
            Component.text("${args.warpName} warp deleted")
                .color(NamedTextColor.GREEN)
        )
    }

    data class Args(
        val warpName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.size != 1) {
                    throw BadCommandUsageException()
                }
                return Args(warpName = args[0])
            }
        }
    }
}