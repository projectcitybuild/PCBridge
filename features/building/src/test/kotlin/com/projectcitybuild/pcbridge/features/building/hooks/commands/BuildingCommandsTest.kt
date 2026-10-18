package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.namespace.SpigotNamespace
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
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
            val command = NightVisionCommand(mock(Plugin::class.java), CoroutineScopeCommandTaskExecutor(this))
            command.removeNightVision = { }
            var enabled = false
            command.enableNightVision = { enabled = true }

            dispatch(command, "nv on", player, PermissionNode.BUILDING_NIGHT_VISION)

            assertTrue(enabled)
            verify(player).sendMessage(l10n.nightVisionToggledOn)
        }

    @Test
    fun `night vision command toggles off when already active`() =
        runTest {
            val player = mock(Player::class.java)
            val command = NightVisionCommand(mock(Plugin::class.java), CoroutineScopeCommandTaskExecutor(this))
            command.hasNightVision = { true }
            var enabled = false
            command.removeNightVision = { }
            command.enableNightVision = { enabled = true }

            dispatch(command, "nv", player, PermissionNode.BUILDING_NIGHT_VISION)

            assertFalse(enabled)
            verify(player).sendMessage(l10n.nightVisionToggledOff)
        }

    @Test
    fun `item name command renames held item and broadcasts rename event`() =
        runTest {
            val player = mock(Player::class.java)
            val item = mock(ItemStack::class.java)
            val meta = mock(ItemMeta::class.java)
            val inventory = mock(PlayerInventory::class.java)
            val broadcaster = mock(SpigotEventBroadcaster::class.java)
            whenever(player.inventory).thenReturn(inventory)
            whenever(inventory.itemInMainHand).thenReturn(item)
            whenever(item.type).thenReturn(Material.STICK)
            whenever(item.itemMeta).thenReturn(meta)

            val command =
                ItemNameCommand(
                    mock(Plugin::class.java),
                    broadcaster,
                    CoroutineScopeCommandTaskExecutor(this),
                )
            dispatch(command, "itemname &aCustom name", player, PermissionNode.BUILDING_ITEM_RENAME)

            verify(meta).displayName(any<Component>())
            verify(item).itemMeta = meta
            verify(player).sendMessage(any<Component>())
            verify(broadcaster).broadcast(any())
        }

    @Test
    fun `invisible frame command places normal frame in inventory and confirms receipt`() =
        runTest {
            val player = mock(Player::class.java)
            val inventory = mock(PlayerInventory::class.java)
            whenever(player.inventory).thenReturn(inventory)
            val namespace = namespace()
            val command =
                InvisFrameCommand(
                    mock(Plugin::class.java),
                    namespace,
                    CoroutineScopeCommandTaskExecutor(this),
                )
            var createdMaterial: Material? = null
            command.frameFactory = { material ->
                createdMaterial = material
                mock(ItemStack::class.java)
            }

            dispatch(command, "invisframe", player, PermissionNode.BUILDING_INVIS_FRAME)

            assertEquals(Material.ITEM_FRAME, createdMaterial)
            verify(inventory).addItem(any())
            verify(player).sendMessage(l10n.receivedInvisFrame)
        }

    @Test
    fun `invisible frame command places glowing frame in inventory and confirms receipt`() =
        runTest {
            val player = mock(Player::class.java)
            val inventory = mock(PlayerInventory::class.java)
            whenever(player.inventory).thenReturn(inventory)
            val command =
                InvisFrameCommand(
                    mock(Plugin::class.java),
                    namespace(),
                    CoroutineScopeCommandTaskExecutor(this),
                )
            var createdMaterial: Material? = null
            command.frameFactory = { material ->
                createdMaterial = material
                mock(ItemStack::class.java)
            }

            dispatch(command, "invisframe glowing", player, PermissionNode.BUILDING_INVIS_FRAME)

            assertEquals(Material.GLOW_ITEM_FRAME, createdMaterial)
            verify(inventory).addItem(any())
            verify(player).sendMessage(l10n.receivedInvisFrameGlowing)
        }

    private suspend fun kotlinx.coroutines.test.TestScope.dispatch(
        command: BrigadierCommand,
        input: String,
        player: Player,
        permission: PermissionNode,
    ) {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(permission.node)).thenReturn(true)
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
        runCurrent()
    }

    private fun namespace(): SpigotNamespace {
        val plugin = mock(JavaPlugin::class.java)
        whenever(plugin.namespace()).thenReturn("pcbridge")
        return SpigotNamespace(plugin)
    }
}
