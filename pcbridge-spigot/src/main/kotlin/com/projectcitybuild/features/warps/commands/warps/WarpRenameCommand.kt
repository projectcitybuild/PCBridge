package com.projectcitybuild.features.warps.commands.warps

import com.projectcitybuild.features.warps.repositories.WarpRepository
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

class WarpRenameCommand(
    private val argsParser: WarpRenameArgs,
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
) {
    suspend fun onCommand(
        sender: CommandSender,
        args: List<String>,
    ) {
        val validated = argsParser.parse(args)

        warpRepository.rename(
            oldName = validated.oldName,
            newName = validated.newName,
        )

        val message = Component.text("${validated.oldName} was renamed to ${validated.newName}")
            .color(NamedTextColor.GREEN)

        audiences.sender(sender).sendMessage(message)
    }
}

class WarpRenameArgs {
    data class Args(
        val oldName: String,
        val newName: String,
    )
    fun parse(args: List<String>): Args {
        check (args.size == 2) {
            "Invalid command arguments"
        }
        return Args(
            oldName = args[0],
            newName = args[1],
        )
    }
}