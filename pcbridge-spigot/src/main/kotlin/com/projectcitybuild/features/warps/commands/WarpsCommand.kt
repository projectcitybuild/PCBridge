package com.projectcitybuild.features.warps.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.projectcitybuild.data.repositories.WarpRepository
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
): SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val warps = warpRepository.all()

        val message = Component.text()
            .content("Test")
            .color(TextColor.color(0x443344))
            .append(
                Component.keybind().keybind("key.jump")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.BOLD, true)
                    .build()
            )
            .build()

        audiences.sender(sender).sendMessage(message)

        // sender.sendMessage(warps.joinToString(separator = ", "))

        return true
    }
}