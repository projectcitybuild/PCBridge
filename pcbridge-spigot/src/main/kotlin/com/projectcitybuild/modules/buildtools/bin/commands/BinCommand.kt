package com.projectcitybuild.modules.buildtools.bin.commands

import com.projectcitybuild.support.spigot.SpigotServer
import org.bukkit.entity.Player

class BinCommand(
    private val spigotServer: SpigotServer,
) {
    fun execute(player: Player) {
        val inventory = spigotServer.createInventory(
            owner = player,
            size = 36,
            title = "Garbage Bin",
        )
        player.openInventory(inventory)
    }
}
