package com.projectcitybuild.modules.buildtools.general.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType

class BinCloseListener : SpigotListener<InventoryCloseEvent> {

    @EventHandler
    override suspend fun handle(event: InventoryCloseEvent) {
        if (event.inventory.type != InventoryType.CHEST) return

        val inventoryTitle = event.view.title
        if (inventoryTitle != "Garbage Bin") return

        val disposedCount = event.inventory.contents
            .filterNotNull()
            .sumOf { it.amount }

        println(event.inventory.contents)

        if (disposedCount == 0) return

        event.player.spigot().sendMessage(
            TextComponent("Disposed of \uD83D\uDD25 $disposedCount items").apply {
                color = ChatColor.GRAY
                isItalic = true
            }
        )
    }
}
