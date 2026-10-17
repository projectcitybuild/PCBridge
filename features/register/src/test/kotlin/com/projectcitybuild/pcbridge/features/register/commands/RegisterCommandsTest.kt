package com.projectcitybuild.pcbridge.features.register.commands

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
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
    private val player = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val playerId = UUID.randomUUID()

    @Test
    fun `code command opens a verification dialog without an email`() =
        runTest {
            setUpContext()
            val command = CodeCommand(mock(Plugin::class.java))
            val dialog = mock(DialogLike::class.java)
            command.dialogFactory = { dialog }

            command.execute(context)

            verify(player).showDialog(dialog)
        }

    @Test
    fun `register command requests a code and opens the email-specific dialog`() =
        runTest {
            val registerService = mock(RegisterHttpService::class.java)
            setUpContext()
            whenever(context.getArgument("email", String::class.java)).thenReturn("user@example.com")
            whenever(player.name).thenReturn("PlayerOne")
            val command = RegisterCommand(mock(Plugin::class.java), registerService)
            val dialog = mock(DialogLike::class.java)
            command.dialogFactory = { email ->
                org.junit.jupiter.api.Assertions.assertEquals("user@example.com", email)
                dialog
            }

            command.execute(context)

            verify(registerService).sendCode(
                playerUUID = playerId,
                playerAlias = "PlayerOne",
                email = "user@example.com",
            )
            verify(player).showDialog(dialog)
        }

    private fun setUpContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/register")
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
    }
}
