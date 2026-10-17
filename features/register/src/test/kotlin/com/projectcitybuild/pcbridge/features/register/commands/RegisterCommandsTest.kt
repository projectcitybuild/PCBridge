package com.projectcitybuild.pcbridge.features.register.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RegisterCommandsTest {
    private val playerId = UUID.randomUUID()

    @Test
    fun `code command opens a verification dialog without an email`() =
        runTest {
            val player = mock(Player::class.java)
            val command = CodeCommand(mock(Plugin::class.java), CoroutineScopeCommandTaskExecutor(this))
            val dialog = mock(DialogLike::class.java)
            command.dialogFactory = { dialog }

            dispatch(command, "code", player)

            verify(player).showDialog(dialog)
        }

    @Test
    fun `register command requests a code and opens the email-specific dialog`() =
        runTest {
            val player = mock(Player::class.java)
            val registerService = mock(RegisterHttpService::class.java)
            val command =
                RegisterCommand(
                    mock(Plugin::class.java),
                    registerService,
                    CoroutineScopeCommandTaskExecutor(this),
                )
            val dialog = mock(DialogLike::class.java)
            command.dialogFactory = { email ->
                org.junit.jupiter.api.Assertions.assertEquals("user@example.com", email)
                dialog
            }
            whenever(player.name).thenReturn("PlayerOne")

            dispatch(command, "register user@example.com", player)

            verify(registerService).sendCode(
                playerUUID = playerId,
                playerAlias = "PlayerOne",
                email = "user@example.com",
            )
            verify(player).showDialog(dialog)
        }

    private suspend fun kotlinx.coroutines.test.TestScope.dispatch(
        command: com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand,
        input: String,
        player: Player,
    ) {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
        runCurrent()
    }
}
