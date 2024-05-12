package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.features.bans.repositories.IPBanRepository
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Success
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.net.InetSocketAddress
import java.util.UUID

class BanIPTest {
    private lateinit var useCase: BanIP
    private lateinit var ipBanRepository: IPBanRepository
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        ipBanRepository = mock(IPBanRepository::class.java)
        server = mock(Server::class.java)

        useCase =
            BanIP(
                ipBanRepository,
                server,
            )
    }

    @Test
    fun `should fail if IP is already banned`() =
        runTest {
            val ip = "127.0.0.1"

            whenever(ipBanRepository.ban(eq(ip), any(), any(), any()))
                .thenThrow(IPBanHttpService.IPAlreadyBannedException::class.java)

            val result =
                useCase.execute(
                    ip = ip,
                    bannerUUID = UUID.randomUUID(),
                    bannerName = "name",
                    reason = "reason",
                )

            assertEquals(Failure(BanIP.FailureReason.IP_ALREADY_BANNED), result)
        }

    @Test
    fun `should fail if IP is invalid`() =
        runTest {
            arrayOf(
                "text",
                "1234",
            ).forEach { invalidIP ->
                val result =
                    useCase.execute(
                        ip = invalidIP,
                        bannerUUID = UUID.randomUUID(),
                        bannerName = "name",
                        reason = "reason",
                    )
                assertEquals(Failure(BanIP.FailureReason.INVALID_IP), result)
            }
        }

    @Test
    fun `should ban valid IP`() =
        runTest {
            val ips =
                arrayOf(
                    "127.0.0.1",
                    "/127.0.0.1:1234", // This should get sanitized
                )
            ips.forEach { ip ->
                val uuid = UUID.randomUUID()
                val result =
                    useCase.execute(
                        ip = ip,
                        bannerUUID = uuid,
                        bannerName = "name",
                        reason = "reason",
                    )
                verify(ipBanRepository).ban(
                    ip = "127.0.0.1",
                    bannerUUID = uuid,
                    bannerName = "name",
                    reason = "reason",
                )
                assertEquals(Success(Unit), result)
            }
        }

    @Test
    fun `should kick player if online`() =
        runTest {
            val otherPlayer = mockPlayer(ip = "127.0.0.2")
            val targetPlayer = mockPlayer(ip = "127.0.0.1")
            whenever(server.onlinePlayers).thenReturn(
                listOf(targetPlayer, otherPlayer),
            )

            useCase.execute(
                ip = "127.0.0.1",
                bannerUUID = UUID.randomUUID(),
                bannerName = "name",
                reason = "reason",
            )

            verify(targetPlayer).kick(any(), eq(PlayerKickEvent.Cause.BANNED))
            verifyNoInteractions(otherPlayer)
        }
}

private fun mockPlayer(ip: String): Player {
    val address = mock(InetSocketAddress::class.java)
    val player = mock(Player::class.java)

    whenever(address.toString()).thenReturn(ip)
    whenever(player.address).thenReturn(address)

    return player
}
