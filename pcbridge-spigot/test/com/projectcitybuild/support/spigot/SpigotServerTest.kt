package com.projectcitybuild.support.spigot

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.net.InetSocketAddress
import java.util.UUID

class SpigotServerTest {
    private lateinit var wrappedServer: Server
    private lateinit var server: SpigotServer

    @BeforeEach
    fun setUp() {
        wrappedServer = mock(Server::class.java)
        server = SpigotServer(wrappedServer)
    }

    @Test
    fun `should kick by player name`() {
        val playerName = "name"

        val otherPlayer = mock(Player::class.java)
        whenever(otherPlayer.name).thenReturn("other_player")

        listOf("name", "NaME", "NAME").forEach { inputName ->
            val reason = "reason"
            val kickContext = SpigotServer.KickContext.FATAL

            val player = mock(Player::class.java)
            whenever(player.name).thenReturn(playerName)
            whenever(wrappedServer.onlinePlayers).thenReturn(listOf(player, otherPlayer))

            server.kickByName(
                playerName = inputName,
                reason = reason,
                context = kickContext,
            )
            verify(player).kickPlayer("${ChatColor.RED}reason")
            verifyNoInteractions(otherPlayer)
        }
    }

    @Test
    fun `should kick by player UUID`() {
        val uuid = UUID.randomUUID()
        val reason = "reason"
        val kickContext = SpigotServer.KickContext.FATAL

        val player = mock(Player::class.java)
        whenever(wrappedServer.getPlayer(uuid)).thenReturn(player)

        server.kickByUUID(
            playerUUID = uuid,
            reason = reason,
            context = kickContext,
        )
        verify(player).kickPlayer("${ChatColor.RED}reason")
    }

    @Test
    fun `should kick by player IP`() {
        val ip = "127.0.0.1"
        val reason = "reason"
        val kickContext = SpigotServer.KickContext.FATAL

        val player = mock(Player::class.java)
        val address = mock(InetSocketAddress::class.java)
        whenever(address.toString()).thenReturn(ip)
        whenever(player.address).thenReturn(address)

        val otherPlayer = mock(Player::class.java)
        val otherAddress = mock(InetSocketAddress::class.java)
        whenever(otherAddress.toString()).thenReturn("192.168.0.1")
        whenever(otherPlayer.address).thenReturn(otherAddress)

        whenever(wrappedServer.onlinePlayers).thenReturn(listOf(player, otherPlayer))

        server.kickByIP(
            ip = ip,
            reason = reason,
            context = kickContext,
        )
        verify(player).kickPlayer("${ChatColor.RED}reason")
        verify(otherPlayer, never()).kickPlayer(anyString())
    }

    @Test
    fun `should broadcast message`() {
        val message = TextComponent("test")

        server.broadcastMessage(message)

        verify(wrappedServer).broadcastMessage(message.toLegacyText())
    }
}