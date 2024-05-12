package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.projectcitybuild.pcbridge.Permissions
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ACommandTest {
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        server = mock(Server::class.java)
    }

    @Test
    fun `only players with permission receive staff messages`() = runTest {
        val regularPlayer = mock(Player::class.java).also {
            whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(false)
        }
        val staffPlayer = mock(Player::class.java).also {
            whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(true)
        }
        whenever(server.onlinePlayers).thenReturn(
            listOf(regularPlayer, staffPlayer),
        )

        val message = "test message"
        val sender = mock(Player::class.java)
        StaffChatCommand(server).run(
            sender = sender,
            args = StaffChatCommand.Args(
                message = message,
            ),
        )

        argumentCaptor<Component>().apply {
            verify(staffPlayer).sendMessage(capture())
            assertTrue(firstValue.toString().contains(message))
        }
        verify(regularPlayer, never()).sendMessage(any<Component>())
    }
}