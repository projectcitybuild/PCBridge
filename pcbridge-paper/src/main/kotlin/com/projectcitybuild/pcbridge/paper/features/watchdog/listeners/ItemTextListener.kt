package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners

import com.projectcitybuild.pcbridge.http.models.discord.DiscordAuthorEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordFieldEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordThumbnailEmbed
import com.projectcitybuild.pcbridge.paper.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.datetime.toISO8601
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
    private val time: LocalizedTime,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onSignChange(event: SignChangeEvent) {
        val serializer = PlainTextComponentSerializer.plainText()
        val lines = event.lines().map { serializer.serialize(it) }

        val embed = DiscordEmbed(
            title = "Sign Edited",
            author = event.player.toDiscordEmbed(),
            description = lines.joinToString(separator = "\n"),
            timestamp = time.now().toISO8601(),
            thumbnail = DiscordThumbnailEmbed("https://minecraft.wiki/images/Invicon_Oak_Sign.png"),
            fields = listOf(
              DiscordFieldEmbed(
                  name = "Location",
                  value = event.block.location.toString(),
              ),
            ),
        )
        discordSend.send(embed)
    }

    // @EventHandler(priority = EventPriority.MONITOR)
    // suspend fun onBookChange(event: PlayerEditBookEvent) {
    //     val message = event.newBookMeta.title
    //     discordSend.send(message)
    // https://minecraft.wiki/images/Book_JE2_BE2.png
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        // Check for a rename when a player and anvil is involved
        val sender = event.whoClicked
        if (sender !is Player || event.inventory !is AnvilInventory) return

        // Ensure they're actually grabbing it out of the anvil slot
        if (event.rawSlot != 2) return

        val displayName = event.currentItem?.itemMeta?.displayName()
            ?: return

        onItemRenamed(
            ItemRenamedEvent(displayName, event.currentItem!!, sender)
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemRenamed(event: ItemRenamedEvent) {
        val displayName = PlainTextComponentSerializer.plainText().serialize(event.displayName)

        val embed = DiscordEmbed(
            title = "Item Renamed",
            author = event.player.toDiscordEmbed(),
            description = displayName,
            thumbnail = DiscordThumbnailEmbed("https://minecraft.wiki/images/Name_Tag_JE2_BE2.png"),
            fields = listOf(
                DiscordFieldEmbed(
                    name = "Item",
                    value = event.item.type.name,
                ),
                DiscordFieldEmbed(
                    name = "Item Quantity",
                    value = event.item.amount.toString(),
                ),
            ),
        )
        discordSend.send(embed)
    }
}

private fun Player.toDiscordEmbed() = DiscordAuthorEmbed(
    name = name,
    iconUrl = "https://minotar.net/avatar/${uniqueId}",
)