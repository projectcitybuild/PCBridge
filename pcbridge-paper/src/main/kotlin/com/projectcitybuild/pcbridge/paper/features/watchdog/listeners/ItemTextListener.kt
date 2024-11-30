package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners

import com.projectcitybuild.pcbridge.http.models.discord.DiscordAuthorEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordFieldEmbed
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.extensions.toISO8601
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.features.building.events.ItemRenamedEvent
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerEditBookEvent
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
            fields = listOf(
              DiscordFieldEmbed(
                  name = "Location",
                  value = event.block.location.toString(),
              ),
            ),
        )
        discordSend.send(embed)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun onBookChange(event: PlayerEditBookEvent) {
        val prevMeta = event.previousBookMeta
        val nextMeta = event.newBookMeta

        if (prevMeta == nextMeta) return

        withContext(Dispatchers.IO) {
            val serializer = PlainTextComponentSerializer.plainText()
            val diff = DiffRowGenerator(
                oldTag = object : DiffTagGenerator {
                    override fun generateOpen(tag: DiffRow.Tag) = "~"
                    override fun generateClose(tag: DiffRow.Tag) = "~"
                },
                newTag = object : DiffTagGenerator {
                    override fun generateOpen(tag: DiffRow.Tag) = "**"
                    override fun generateClose(tag: DiffRow.Tag) = "**"
                },
            ).generateDiffRows(
                original = prevMeta.pages().map(serializer::serialize),
                revised = nextMeta.pages().map(serializer::serialize),
            )

            val embed = DiscordEmbed(
                title = "Book Edited",
                author = event.player.toDiscordEmbed(),
                description = diff.joinToString(separator = "\n---\n") { "[OLD] ${it.oldLine}\n[NEW] ${it.newLine}" },
                timestamp = time.now().toISO8601(),
                fields = mutableListOf<DiscordFieldEmbed>().also {
                    if (nextMeta.title != null) {
                        it.add(DiscordFieldEmbed(
                            name = "Title",
                            value = nextMeta.title,
                        ))
                    }
                    if (nextMeta.author != null) {
                        it.add(DiscordFieldEmbed(
                            name = "Book Author",
                            value = nextMeta.author,
                        ))
                    }
                },
            )
            discordSend.send(embed)
        }
    }


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