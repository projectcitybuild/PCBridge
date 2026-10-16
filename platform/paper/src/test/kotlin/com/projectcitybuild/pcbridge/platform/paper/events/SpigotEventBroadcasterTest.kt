package com.projectcitybuild.pcbridge.platform.paper.events

import com.projectcitybuild.pcbridge.core.testsupport.RecordingDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SpigotEventBroadcasterTest {
    @Test
    fun `broadcast should call plugin manager using minecraft dispatcher`() =
        runTest {
            val server = mock<Server>()
            val pluginManager = mock<PluginManager>()
            whenever(server.pluginManager).thenReturn(pluginManager)

            val dispatcher = RecordingDispatcher(StandardTestDispatcher(testScheduler))
            var dispatcherRequests = 0
            val event = mock<Event>()
            val broadcaster = SpigotEventBroadcaster(server) {
                dispatcherRequests++
                dispatcher
            }

            broadcaster.broadcast(event)

            assertEquals(1, dispatcherRequests)
            assertTrue(dispatcher.dispatchCount > 0)
            verify(pluginManager).callEvent(event)
        }
}
