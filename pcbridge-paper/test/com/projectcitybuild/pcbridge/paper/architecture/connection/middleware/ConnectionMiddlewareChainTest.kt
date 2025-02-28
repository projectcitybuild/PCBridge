package com.projectcitybuild.pcbridge.paper.architecture.connection.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.InetAddress
import java.util.UUID

class ConnectionMiddlewareChainTest {
    private val uuid: UUID get() = UUID.randomUUID()
    private val address: InetAddress get() = mock(InetAddress::class.java)
    private val playerData: PlayerData get() = PlayerData()
    private val denyReason = Component.text("foobar")

    suspend fun alwaysAllow(): ConnectionMiddleware {
        return mock(ConnectionMiddleware::class.java).also {
            whenever(it.handle(any(), any(), any())).thenReturn(
                ConnectionResult.Allowed
            )
        }
    }

    suspend fun alwaysDeny(): ConnectionMiddleware {
        return mock(ConnectionMiddleware::class.java).also {
            whenever(it.handle(any(), any(), any())).thenReturn(
                ConnectionResult.Denied(denyReason)
            )
        }
    }

    @Test
    fun `returns first middleware that denies`() = runTest {
        val firstAllow = alwaysAllow()
        val secondAllow = alwaysAllow()
        val firstDeny = alwaysDeny()
        val secondDeny = alwaysDeny()

        val chain = ConnectionMiddlewareChain(
            middlewares = mutableListOf(
                firstAllow,
                firstDeny,
                secondAllow,
                secondDeny,
            )
        )
        val result = chain.pipe(uuid, address, playerData)

        assertTrue(result is ConnectionResult.Denied)

        verify(firstAllow, times(1)).handle(any(), any(), any())
        verify(firstDeny, times(1)).handle(any(), any(), any())
        verify(secondAllow, times(0)).handle(any(), any(), any())
        verify(secondDeny, times(0)).handle(any(), any(), any())
    }

    @Test
    fun `allows if no middleware denies`() = runTest {
        val chain = ConnectionMiddlewareChain(
            middlewares = mutableListOf(
                alwaysAllow(),
                alwaysAllow(),
                alwaysAllow(),
            )
        )
        val result = chain.pipe(uuid, address, playerData)

        assertTrue(result is ConnectionResult.Allowed)
    }

    @Test
    fun `allows if no middleware registered`() = runTest {
        val chain = ConnectionMiddlewareChain(
            middlewares = mutableListOf()
        )
        val result = chain.pipe(uuid, address, playerData)

        assertTrue(result is ConnectionResult.Allowed)
    }
}