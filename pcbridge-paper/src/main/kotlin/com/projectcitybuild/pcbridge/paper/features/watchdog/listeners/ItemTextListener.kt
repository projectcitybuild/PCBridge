package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners

import com.projectcitybuild.pcbridge.http.discord.models.DiscordAuthorEmbed
import com.projectcitybuild.pcbridge.http.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.http.discord.models.DiscordFieldEmbed
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.extensions.toISO8601
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.features.building.domain.data.ItemRenamedEvent
import com.projectcitybuild.pcbridge.paper.features.watchdog.watchDogTracer
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
    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true,
    )
    fun onSignChange(
        event: SignChangeEvent,
    ) = event.scopedSync(watchDogTracer, this::class.java) {
        val serializer = PlainTextComponentSerializer.plainText()
        val lines = event.lines().mapNotNull { serializer.serialize(it) }
        val description = lines.joinToString(separator = "\n")

        if (lines.isEmpty() || description.isEmpty()) return@scopedSync

        val embed = DiscordEmbed(
            title = "Sign Edited",
            author = event.player.toDiscordEmbed(),
            description = description,
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

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true,
    )
    suspend fun onBookChange(
        event: PlayerEditBookEvent,
    ) = event.scoped(watchDogTracer, this::class.java) {
        val prevMeta = event.previousBookMeta
        val nextMeta = event.newBookMeta

        if (prevMeta == nextMeta) return@scoped

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
                        it.add(
                            DiscordFieldEmbed(
                                name = "Title",
                                value = nextMeta.title,
                            )
                        )
                    }
                    if (nextMeta.author != null) {
                        it.add(
                            DiscordFieldEmbed(
                                name = "Book Author",
                                value = nextMeta.author,
                            )
                        )
                    }
                },
            )
            discordSend.send(embed)
        }
    }


    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true,
    )
    fun onInventoryClick(
        event: InventoryClickEvent,
    ) = event.scopedSync(watchDogTracer, this::class.java) {
        // Check for a rename when a player and anvil is involved
        val sender = event.whoClicked
        if (sender !is Player || event.inventory !is AnvilInventory) return@scopedSync

        // Ensure they're actually grabbing it out of the anvil slot
        if (event.rawSlot != 2) return@scopedSync

        val displayName = event.currentItem?.itemMeta?.displayName()
            ?: return@scopedSync

        onItemRenamed(
            ItemRenamedEvent(displayName, event.currentItem!!, sender)
        )
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true,
    )
    fun onItemRenamed(
        event: ItemRenamedEvent,
    ) = event.scopedSync(watchDogTracer, this::class.java) {
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