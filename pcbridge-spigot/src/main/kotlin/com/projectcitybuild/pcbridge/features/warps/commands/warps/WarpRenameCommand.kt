package com.projectcitybuild.pcbridge.features.warps.commands.warps

import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

class WarpRenameCommand(
    private val warpRepository: WarpRepository,
): SpigotCommand<WarpRenameCommand.Args> {
    override val label = "rename"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        if (!sender.hasPermission("pcbridge.warp.manage")) {
            throw UnauthorizedCommandException()
        }
        warpRepository.rename(
            oldName = args.oldName,
            newName = args.newName,
        )
        sender.sendMessage(
            Component.text("${args.oldName} renamed to ${args.newName}")
                .color(NamedTextColor.GREEN)
        )
    }

    data class Args(
        val oldName: String,
        val newName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.size != 2) {
                    throw BadCommandUsageException()
                }
                return Args(
                    oldName = args[0],
                    newName = args[1],
                )
            }
        }
    }
}