package com.projectcitybuild.plugin.commands

import com.projectcitybuild.modules.buildtools.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.support.spigot.SpigotNamespace
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import kotlinx.coroutines.test.runTest
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class InvisFrameCommandTest {

    private lateinit var inventory: PlayerInventory
    private lateinit var player: Player
    private lateinit var command: InvisFrameCommand

    @BeforeEach
    fun setUp() {
        val spigotNamespace = mock(SpigotNamespace::class.java)

        inventory = mock(PlayerInventory::class.java)
        player = mock(Player::class.java)
        command = InvisFrameCommand(spigotNamespace)

        whenever(player.spigot())
            .thenReturn(mock(Player.Spigot::class.java))

        whenever(player.inventory)
            .thenReturn(inventory)

        whenever(spigotNamespace.invisibleKey)
            .thenReturn(mock(NamespacedKey::class.java))
    }

    @Test
    fun `throws exception if console uses command`() = runTest {
        val input = SpigotCommandInput(
            sender = player,
            args = emptyList(),
            isConsole = true,
        )
        assertThrows<CannotInvokeFromConsoleException> {
            command.execute(input)
        }
    }

    @Test
    fun `throws exception if too many arguments`() = runTest {
        val input = SpigotCommandInput(
            sender = player,
            args = listOf("test"),
            isConsole = false,
        )
        assertThrows<InvalidCommandArgumentsException> {
            command.execute(input)
        }
    }

    @Test
    fun `throws exception if invalid option`() = runTest {
        listOf("bad", "1").forEach { invalidOption ->
            val input = SpigotCommandInput(
                sender = player,
                args = listOf(invalidOption),
                isConsole = false,
            )
            assertThrows<InvalidCommandArgumentsException> {
                command.execute(input)
            }
        }
        // listOf("glowing", "GLOWING", "glOwIng").forEach { validOption ->
        //     val input = SpigotCommandInput(
        //         sender = player,
        //         args = listOf(validOption),
        //         isConsole = false,
        //     )
        //     assertDoesNotThrow {
        //         command.execute(input)
        //     }
        // }
    }
}
