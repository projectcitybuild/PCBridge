package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.GameBanMock
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Plugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.*

class BanConnectionListenerTest {

    private lateinit var listener: BanConnectionListener
    private lateinit var scheduler: TestCoroutineScheduler

    private val plugin = mock(Plugin::class.java)
    private val banRepository = mock(BanRepository::class.java)
    private val logger = mock(PlatformLogger::class.java)
    private val errorReporter = mock(ErrorReporter::class.java)

    @BeforeEach
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        listener = BanConnectionListener(
            plugin,
            banRepository,
            logger,
            DateTimeFormatterMock(),
            errorReporter,
        ).apply {
            dispatcher = StandardTestDispatcher(scheduler)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        val playerUUID = UUID.randomUUID()
        val event = mock(LoginEvent::class.java)
        val connection = mock(PendingConnection::class.java)

        `when`(banRepository.get(playerUUID)).thenReturn(GameBanMock())
        `when`(event.connection).thenReturn(connection)
        `when`(connection.uniqueId).thenReturn(playerUUID)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event).isCancelled = true
        verify(event).setCancelReason(any(BaseComponent::class.java))
    }

    @Test
    fun `does not cancel login event if player is not banned`() = runTest {
        val playerUUID = UUID.randomUUID()
        val event = mock(LoginEvent::class.java)
        val connection = mock(PendingConnection::class.java)

        `when`(banRepository.get(playerUUID)).thenReturn(null)
        `when`(event.connection).thenReturn(connection)
        `when`(connection.uniqueId).thenReturn(playerUUID)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event, never()).isCancelled
        verify(event, never()).setCancelReason(any(BaseComponent::class.java))
    }
}