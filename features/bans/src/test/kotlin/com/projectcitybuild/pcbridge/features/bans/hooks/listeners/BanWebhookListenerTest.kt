package com.projectcitybuild.pcbridge.features.bans.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.IPBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.LocalDateTime
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Player as ApiPlayer

class BanWebhookListenerTest {
    private val server = mock(Server::class.java)
    private val listener = BanWebhookListener(server)

    @Test
    fun `kicks and broadcasts for an online player matching an ip ban`() {
        val player = mock(Player::class.java)
        whenever(player.address).thenReturn(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 25565))
        whenever(player.name).thenReturn("Steve")
        whenever(server.onlinePlayers).thenReturn(setOf(player))
        val ban =
            IPBan(
                id = 1,
                ipAddress = "127.0.0.1",
                bannerPlayerId = 10,
                reason = "Abuse",
                createdAt = LocalDateTime.parse("2024-01-01T00:00:00"),
                updatedAt = LocalDateTime.parse("2024-01-01T00:00:00"),
            )

        listener.onIpBanned(
            WebhookReceivedEvent(
                WebhookEvent.IPBanRequested(ban),
            ),
        )

        verify(player).kick(any(), eq(PlayerKickEvent.Cause.IP_BANNED))
        verify(server).broadcast(any<Component>())
    }

    @Test
    fun `ignores webhooks that do not request an ip ban`() {
        listener.onIpBanned(
            WebhookReceivedEvent(
                WebhookEvent.SyncWarps(emptyList()),
            ),
        )
        verify(server, never()).broadcast(any<Component>())
    }

    @Test
    fun `kicks and broadcasts for an online player matching a uuid ban`() {
        val id = java.util.UUID.randomUUID()
        val onlinePlayer = mock(Player::class.java)
        whenever(onlinePlayer.uniqueId).thenReturn(id)
        whenever(onlinePlayer.name).thenReturn("Steve")
        whenever(server.onlinePlayers).thenReturn(setOf(onlinePlayer))
        val ban =
            PlayerBan(
                id = 7,
                bannedPlayer = ApiPlayer(id = 1, uuid = id.toString()),
                reason = "Abuse",
                createdAt = LocalDateTime.parse("2024-01-01T00:00:00"),
                updatedAt = LocalDateTime.parse("2024-01-01T00:00:00"),
            )

        listener.onUuidBanned(
            WebhookReceivedEvent(
                WebhookEvent.UUIDBanRequested(ban),
            ),
        )

        verify(onlinePlayer).kick(
            any(),
            eq(PlayerKickEvent.Cause.BANNED),
        )
        verify(server).broadcast(any<Component>())
    }
}
