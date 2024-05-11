package com.projectcitybuild.features.commands

import com.projectcitybuild.Permissions
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ACommandTest {
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        server = mock(Server::class.java)
    }

    @Test
    fun `only players with permission receive staff messages`() {
        val regularPlayerChat = mock(Player.Spigot::class.java)
        val regularPlayer = mock(Player::class.java).also {
            whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(false)
            whenever(it.spigot()).thenReturn(regularPlayerChat)
        }
        val staffPlayerChat = mock(Player.Spigot::class.java)
        val staffPlayer = mock(Player::class.java).also {
            whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(true)
            whenever(it.spigot()).thenReturn(staffPlayerChat)
        }
        whenever(server.onlinePlayers).thenReturn(listOf(regularPlayer, staffPlayer))

        val message = "test message"
        val sender = mock(Player::class.java)
        ACommand(server).execute(sender, message)

        argumentCaptor<TextComponent>().apply {
            verify(staffPlayerChat).sendMessage(capture())
            assertTrue(firstValue.toString().contains(message))
        }
        verifyNoInteractions(regularPlayerChat)
    }
}