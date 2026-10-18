package com.projectcitybuild.pcbridge.features.workstations.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.features.workstations.WorkstationsPermissions
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
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
    private val source = mock(CommandSourceStack::class.java)

    @Test
    fun `anvil command opens anvil for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("anvil", "anvil", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                AnvilCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `cartography command opens cartography table for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("cartographytable", "cartography", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                CartographyTableCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `enchanting command opens enchanting table for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("enchanting", "enchanting", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                EnchantingCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `grindstone command opens grindstone for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("grindstone", "grindstone", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                GrindstoneCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `loom command opens loom for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("loom", "loom", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                LoomCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `smithing command opens smithing table for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("smithingtable", "smithing", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                SmithingTableCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `stonecutter command opens stonecutter for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("stonecutter", "stonecutter", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                StoneCutterCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    @Test
    fun `workbench command opens crafting table for the invoking player and checks permission`() =
        runTest {
            assertCommandBehavior("workbench", "workbench", CoroutineScopeCommandTaskExecutor(this)) { executor, opener ->
                WorkbenchCommand(mock(Plugin::class.java), executor).also { it.menuOpener = opener }
            }
        }

    private suspend fun TestScope.assertCommandBehavior(
        name: String,
        menuType: String,
        executor: CommandTaskExecutor,
        createCommand: (CommandTaskExecutor, (String, Player) -> Unit) -> BrigadierCommand,
    ) {
        val openedMenus = mutableListOf<Pair<String, Player>>()
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(WorkstationsPermissions.USE.node)).thenReturn(true)
        val command =
            createCommand(executor) { actualType, actualPlayer ->
                openedMenus.add(actualType to actualPlayer)
            }
        val node = command.literal()

        assertEquals(name, node.name)
        assertTrue(node.requirement.test(source))
        whenever(player.hasPermission(WorkstationsPermissions.USE.node)).thenReturn(false)
        assertFalse(node.requirement.test(source))
        whenever(player.hasPermission(WorkstationsPermissions.USE.node)).thenReturn(true)

        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(node)
        dispatcher.execute(name, source)
        runCurrent()

        assertEquals(listOf(menuType to player), openedMenus)
        assertSame(player, openedMenus.single().second)
    }
}
