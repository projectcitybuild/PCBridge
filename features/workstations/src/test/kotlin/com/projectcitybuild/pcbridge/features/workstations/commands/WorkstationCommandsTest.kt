package com.projectcitybuild.pcbridge.features.workstations.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class WorkstationCommandsTest {
    private val player = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val source = mock(CommandSourceStack::class.java)

    @Test
    fun `anvil command opens anvil for the invoking player and checks permission`() =
        runTest {
            val command = AnvilCommand(mock(Plugin::class.java))
            assertCommandBehavior("anvil", command.literal(), "anvil") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `cartography command opens cartography table for the invoking player and checks permission`() =
        runTest {
            val command = CartographyTableCommand(mock(Plugin::class.java))
            assertCommandBehavior("cartographytable", command.literal(), "cartography") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `enchanting command opens enchanting table for the invoking player and checks permission`() =
        runTest {
            val command = EnchantingCommand(mock(Plugin::class.java))
            assertCommandBehavior("enchanting", command.literal(), "enchanting") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `grindstone command opens grindstone for the invoking player and checks permission`() =
        runTest {
            val command = GrindstoneCommand(mock(Plugin::class.java))
            assertCommandBehavior("grindstone", command.literal(), "grindstone") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `loom command opens loom for the invoking player and checks permission`() =
        runTest {
            val command = LoomCommand(mock(Plugin::class.java))
            assertCommandBehavior("loom", command.literal(), "loom") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `smithing command opens smithing table for the invoking player and checks permission`() =
        runTest {
            val command = SmithingTableCommand(mock(Plugin::class.java))
            assertCommandBehavior("smithingtable", command.literal(), "smithing") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `stonecutter command opens stonecutter for the invoking player and checks permission`() =
        runTest {
            val command = StoneCutterCommand(mock(Plugin::class.java))
            assertCommandBehavior("stonecutter", command.literal(), "stonecutter") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    @Test
    fun `workbench command opens crafting table for the invoking player and checks permission`() =
        runTest {
            val command = WorkbenchCommand(mock(Plugin::class.java))
            assertCommandBehavior("workbench", command.literal(), "workbench") { opener ->
                command.menuOpener = opener
                command.execute(context)
            }
        }

    private suspend fun assertCommandBehavior(
        name: String,
        node: com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode,
        menuType: String,
        execute: suspend ((String, Player) -> Unit) -> Unit,
    ) {
        val openedMenus = mutableListOf<Pair<String, Player>>()
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/$name")
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(PermissionNode.WORKSTATIONS_USE.node)).thenReturn(true)

        assertEquals(name, node.name)
        assertTrue(node.requirement.test(source))
        whenever(player.hasPermission(PermissionNode.WORKSTATIONS_USE.node)).thenReturn(false)
        assertFalse(node.requirement.test(source))
        whenever(player.hasPermission(PermissionNode.WORKSTATIONS_USE.node)).thenReturn(true)

        execute { actualType, actualPlayer -> openedMenus.add(actualType to actualPlayer) }

        assertEquals(listOf(menuType to player), openedMenus)
        assertSame(player, openedMenus.single().second)
    }
}
