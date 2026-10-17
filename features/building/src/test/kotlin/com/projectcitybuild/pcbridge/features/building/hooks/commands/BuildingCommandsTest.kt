package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.namespace.SpigotNamespace
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class BuildingCommandsTest {
    @Test
    fun `night vision command enables the effect when requested`() =
        runTest {
            val player = mock(Player::class.java)
            val context = playerContext(player)
            whenever(context.getArgument("enabled", Boolean::class.java)).thenReturn(true)
            var removed = false
            var enabled = false
            val command = NightVisionCommand(mock(Plugin::class.java))
            command.removeNightVision = { removed = true }
            command.enableNightVision = { enabled = true }

            command.execute(context)

            assertTrue(removed)
            assertTrue(enabled)
            verify(player).sendMessage(l10n.nightVisionToggledOn)
        }

    @Test
    fun `night vision command toggles off when already active`() =
        runTest {
            val player = mock(Player::class.java)
            val context = playerContext(player)
            whenever(context.getArgument("enabled", Boolean::class.java)).thenThrow(IllegalArgumentException())
            var removed = false
            var enabled = false
            val command = NightVisionCommand(mock(Plugin::class.java))
            command.hasNightVision = { true }
            command.removeNightVision = { removed = true }
            command.enableNightVision = { enabled = true }

            command.execute(context)

            assertTrue(removed)
            assertFalse(enabled)
            verify(player).sendMessage(l10n.nightVisionToggledOff)
        }

    @Test
    fun `item name command renames held item and broadcasts rename event`() =
        runTest {
            val player = mock(Player::class.java)
            val context = playerContext(player)
            val item = mock(ItemStack::class.java)
            val meta = mock(org.bukkit.inventory.meta.ItemMeta::class.java)
            val inventory = mock(org.bukkit.inventory.PlayerInventory::class.java)
            val broadcaster = mock(SpigotEventBroadcaster::class.java)
            whenever(player.inventory).thenReturn(inventory)
            whenever(inventory.itemInMainHand).thenReturn(item)
            whenever(item.type).thenReturn(Material.STICK)
            whenever(item.itemMeta).thenReturn(meta)
            whenever(context.getArgument("name", String::class.java)).thenReturn("&aCustom name")

            ItemNameCommand(mock(Plugin::class.java), broadcaster).execute(context)

            verify(meta).displayName(any<Component>())
            verify(item).setItemMeta(meta)
            verify(player).sendMessage(any<Component>())
            verify(broadcaster).broadcast(any())
        }

    @Test
    fun `invisible frame command places normal frame in inventory and confirms receipt`() {
        val player = mock(Player::class.java)
        val inventory = mock(org.bukkit.inventory.PlayerInventory::class.java)
        whenever(player.inventory).thenReturn(inventory)
        val plugin = mock(JavaPlugin::class.java)
        whenever(plugin.namespace()).thenReturn("pcbridge")
        val command = InvisFrameCommand(mock(Plugin::class.java), SpigotNamespace(plugin))
        var createdMaterial: Material? = null
        command.frameFactory = { material ->
            createdMaterial = material
            mock(ItemStack::class.java)
        }

        command.giveNormal(playerContext(player))

        assertEquals(Material.ITEM_FRAME, createdMaterial)
        verify(inventory).addItem(any())
        verify(player).sendMessage(l10n.receivedInvisFrame)
    }

    @Test
    fun `invisible frame command places glowing frame in inventory and confirms receipt`() {
        val player = mock(Player::class.java)
        val inventory = mock(org.bukkit.inventory.PlayerInventory::class.java)
        whenever(player.inventory).thenReturn(inventory)
        val plugin = mock(JavaPlugin::class.java)
        whenever(plugin.namespace()).thenReturn("pcbridge")
        val command = InvisFrameCommand(mock(Plugin::class.java), SpigotNamespace(plugin))
        var createdMaterial: Material? = null
        command.frameFactory = { material ->
            createdMaterial = material
            mock(ItemStack::class.java)
        }

        command.giveGlowing(playerContext(player))

        assertEquals(Material.GLOW_ITEM_FRAME, createdMaterial)
        verify(inventory).addItem(any())
        verify(player).sendMessage(l10n.receivedInvisFrameGlowing)
    }

    private fun playerContext(player: Player): PaperCommandContext {
        val context = mock(PaperCommandContext::class.java)
        val source = mock(CommandSourceStack::class.java)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/test")
        whenever(source.sender).thenReturn(player)
        return context
    }
}
