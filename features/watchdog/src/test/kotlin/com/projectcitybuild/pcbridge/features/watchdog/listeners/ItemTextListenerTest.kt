package com.projectcitybuild.pcbridge.features.watchdog.listeners

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.discord.DiscordSend
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.features.building.domain.data.ItemRenamedEvent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class ItemTextListenerTest {
    private val discordSend = mock(DiscordSend::class.java)
    private val localizedTime = mock(LocalizedTime::class.java)
    private val listener = ItemTextListener(discordSend, localizedTime)

    @Test
    fun `sends a discord embed for a non-empty sign edit`() {
        val player = mockPlayer()
        val event = mock(SignChangeEvent::class.java)
        val block = mock(Block::class.java)
        val location = Location(null, 2.0, 64.0, 3.0)
        whenever(event.lines()).thenReturn(listOf(Component.text("Welcome"), Component.text("Builders")))
        whenever(event.player).thenReturn(player)
        whenever(event.block).thenReturn(block)
        whenever(block.location).thenReturn(location)
        whenever(localizedTime.now()).thenReturn(LocalDateTime.of(2025, 1, 2, 3, 4))

        listener.onSignChange(event)

        val embed = argumentCaptor<DiscordEmbed>()
        verify(discordSend).send(embed.capture())
        assertEquals("Sign Edited", embed.firstValue.title)
        assertEquals("Welcome\nBuilders", embed.firstValue.description)
        assertEquals(location.toString(), requireNotNull(embed.firstValue.fields).single().value)
    }

    @Test
    fun `does not send an embed when a sign has no text`() {
        val event = mock(SignChangeEvent::class.java)
        whenever(event.lines()).thenReturn(emptyList())

        listener.onSignChange(event)

        verify(discordSend, never()).send(org.mockito.kotlin.any())
    }

    @Test
    fun `does not send an embed when all sign lines serialize to empty text`() {
        val event = mock(SignChangeEvent::class.java)
        whenever(event.lines()).thenReturn(listOf(Component.empty()))

        listener.onSignChange(event)

        verify(discordSend, never()).send(org.mockito.kotlin.any())
    }

    @Test
    fun `sends changed book pages and non-empty book metadata`() =
        runTest {
            val player = mockPlayer()
            val previous = mock(BookMeta::class.java)
            val next = mock(BookMeta::class.java)
            whenever(previous.pages()).thenReturn(listOf(Component.text("Before")))
            whenever(next.pages()).thenReturn(listOf(Component.text("After")))
            whenever(next.title).thenReturn("A title")
            whenever(next.author).thenReturn("An author")
            val event = mock(PlayerEditBookEvent::class.java)
            whenever(event.previousBookMeta).thenReturn(previous)
            whenever(event.newBookMeta).thenReturn(next)
            whenever(event.player).thenReturn(player)
            whenever(localizedTime.now()).thenReturn(LocalDateTime.of(2025, 1, 2, 3, 4))

            listener.onBookChange(event)

            val embed = argumentCaptor<DiscordEmbed>()
            verify(discordSend).send(embed.capture())
            assertEquals("Book Edited", embed.firstValue.title)
            assertTrue(requireNotNull(embed.firstValue.description).contains("Before"))
            assertTrue(requireNotNull(embed.firstValue.description).contains("After"))
            assertEquals(
                listOf("A title", "An author"),
                requireNotNull(embed.firstValue.fields).map { it.value },
            )
        }

    @Test
    fun `does not send a book embed when the metadata is unchanged`() =
        runTest {
            val metadata = mock(BookMeta::class.java)
            val event = mock(PlayerEditBookEvent::class.java)
            whenever(event.previousBookMeta).thenReturn(metadata)
            whenever(event.newBookMeta).thenReturn(metadata)

            listener.onBookChange(event)

            verify(discordSend, never()).send(org.mockito.kotlin.any())
        }

    @Test
    fun `sends a changed book without metadata fields when title and author are absent`() =
        runTest {
            val previous = mock(BookMeta::class.java)
            val next = mock(BookMeta::class.java)
            whenever(previous.pages()).thenReturn(listOf(Component.text("Old page")))
            whenever(next.pages()).thenReturn(listOf(Component.text("New page")))
            val player = mockPlayer()
            whenever(localizedTime.now()).thenReturn(LocalDateTime.of(2025, 1, 2, 3, 4))
            val event = mock(PlayerEditBookEvent::class.java)
            whenever(event.previousBookMeta).thenReturn(previous)
            whenever(event.newBookMeta).thenReturn(next)
            whenever(event.player).thenReturn(player)

            listener.onBookChange(event)

            val embed = argumentCaptor<DiscordEmbed>()
            verify(discordSend).send(embed.capture())
            assertTrue(requireNotNull(embed.firstValue.fields).isEmpty())
        }

    @Test
    fun `only reports an item name when taken from an anvil result slot`() {
        val player = mockPlayer()
        val inventory = mock(AnvilInventory::class.java)
        val item = mock(ItemStack::class.java)
        val itemMeta = mock(ItemMeta::class.java)
        val clickEvent = mock(InventoryClickEvent::class.java)
        whenever(clickEvent.whoClicked).thenReturn(player)
        whenever(clickEvent.inventory).thenReturn(inventory)
        whenever(clickEvent.rawSlot).thenReturn(2)
        whenever(clickEvent.currentItem).thenReturn(item)
        whenever(item.itemMeta).thenReturn(itemMeta)
        whenever(itemMeta.displayName()).thenReturn(Component.text("Named item"))
        whenever(item.type).thenReturn(org.bukkit.Material.DIAMOND)
        whenever(item.amount).thenReturn(1)

        listener.onInventoryClick(clickEvent)

        val embed = argumentCaptor<DiscordEmbed>()
        verify(discordSend).send(embed.capture())
        assertEquals("Item Renamed", embed.firstValue.title)
        assertEquals("Named item", embed.firstValue.description)
        assertEquals(listOf("DIAMOND", "1"), requireNotNull(embed.firstValue.fields).map { it.value })
    }

    @Test
    fun `ignores clicks outside the anvil result slot`() {
        val player = mockPlayer()
        val event = mock(InventoryClickEvent::class.java)
        whenever(event.whoClicked).thenReturn(player)
        whenever(event.inventory).thenReturn(mock(AnvilInventory::class.java))
        whenever(event.rawSlot).thenReturn(1)

        listener.onInventoryClick(event)

        verify(discordSend, never()).send(org.mockito.kotlin.any())
    }

    @Test
    fun `ignores clicks without a player anvil result and custom item name`() {
        val nonPlayerEvent = mock(InventoryClickEvent::class.java)
        whenever(nonPlayerEvent.whoClicked).thenReturn(mock(HumanEntity::class.java))
        listener.onInventoryClick(nonPlayerEvent)

        val player = mockPlayer()
        val regularInventoryEvent = mock(InventoryClickEvent::class.java)
        val regularInventory = mock(Inventory::class.java)
        whenever(regularInventoryEvent.whoClicked).thenReturn(player)
        whenever(regularInventoryEvent.inventory).thenReturn(regularInventory)
        listener.onInventoryClick(regularInventoryEvent)

        val unnamedItem = mock(ItemStack::class.java)
        val unnamedMeta = mock(ItemMeta::class.java)
        val anvil = mock(AnvilInventory::class.java)
        whenever(unnamedItem.itemMeta).thenReturn(unnamedMeta)
        val noNameEvent = mock(InventoryClickEvent::class.java)
        whenever(noNameEvent.whoClicked).thenReturn(player)
        whenever(noNameEvent.inventory).thenReturn(anvil)
        whenever(noNameEvent.rawSlot).thenReturn(2)
        whenever(noNameEvent.currentItem).thenReturn(unnamedItem)
        listener.onInventoryClick(noNameEvent)

        verify(discordSend, never()).send(org.mockito.kotlin.any())
    }

    @Test
    fun `reports a directly raised item rename`() {
        val player = mockPlayer()
        val item = mock(ItemStack::class.java)
        whenever(item.type).thenReturn(org.bukkit.Material.STONE)
        whenever(item.amount).thenReturn(3)

        listener.onItemRenamed(ItemRenamedEvent(Component.text("Stone stack"), item, player))

        val embed = argumentCaptor<DiscordEmbed>()
        verify(discordSend).send(embed.capture())
        assertEquals("Stone stack", embed.firstValue.description)
        assertEquals(listOf("STONE", "3"), requireNotNull(embed.firstValue.fields).map { it.value })
    }

    private fun mockPlayer(): Player {
        return mock(Player::class.java).also {
            whenever(it.name).thenReturn("Player")
            whenever(it.uniqueId).thenReturn(UUID.randomUUID())
        }
    }
}
