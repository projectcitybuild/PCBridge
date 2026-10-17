package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class VanillaOpInterceptListenerTest {
    private val listener = VanillaOpInterceptListener()

    @Test
    fun `cancels op and deop player commands case insensitively`() {
        listOf("/op target", "/DEOP target ").forEach { message ->
            val event = mock(PlayerCommandPreprocessEvent::class.java)
            whenever(event.message).thenReturn(message)

            listener.onPlayerCommandPreprocess(event)

            verify(event).isCancelled = true
        }
    }

    @Test
    fun `does not cancel unrelated commands or op words`() {
        listOf("/open", "/deopper", "/say hello").forEach { message ->
            val event = mock(PlayerCommandPreprocessEvent::class.java)
            whenever(event.message).thenReturn(message)

            listener.onPlayerCommandPreprocess(event)

            verify(event, never()).isCancelled = true
        }
    }
}
