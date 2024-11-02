package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners

import com.projectcitybuild.pcbridge.paper.core.discord.services.DiscordSend
import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.events.ItemRenamedEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.AnvilInventory

class ItemTextListener(
    private val discordSend: DiscordSend,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun onSignChange(event: SignChangeEvent) {
        val serializer = PlainTextComponentSerializer.plainText()
        val lines = event.lines().map { serializer.serialize(it) }
        val message = lines.joinToString(separator = "\n")

        discordSend.send(message)
    }

    // @EventHandler(priority = EventPriority.MONITOR)
    // suspend fun onBookChange(event: PlayerEditBookEvent) {
    //     val message = event.newBookMeta.title
    //     discordSend.send(message)
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onInventoryClick(event: InventoryClickEvent) {
        // Check for a rename when a player and anvil is involved
        if (event.whoClicked !is Player || event.inventory !is AnvilInventory) return

        // Ensure they're actually grabbing it out of the anvil slot
        if (event.rawSlot != 2) return

        val displayName = event.currentItem?.itemMeta?.displayName()
            ?: return

        onItemRenamed(ItemRenamedEvent(displayName))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun onItemRenamed(event: ItemRenamedEvent) {
        val message = PlainTextComponentSerializer.plainText().serialize(event.displayName)
        discordSend.send(message)
    }
}