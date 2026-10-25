package com.projectcitybuild.pcbridge.runtime.remoteconfig

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.ConfigHttpService
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

class RemoteConfigTest {
    @Test
    fun `initialization establishes snapshot without broadcasting`() =
        runTest {
            val first = RemoteConfigVersion(1, RemoteConfigKeyValues())
            val next = RemoteConfigVersion(2, RemoteConfigKeyValues(motd = "updated"))
            val service = mock<ConfigHttpService>()
            val broadcaster = mock<SpigotEventBroadcaster>()
            val storage = mock<FileStorage<RemoteConfigVersion>>()
            whenever(service.get()).thenReturn(first, next)
            val remoteConfig = RemoteConfig(service, broadcaster, File("unused.json"), storage, mock())

            assertEquals(first, remoteConfig.initialize())
            verify(broadcaster, never()).broadcast(any())

            assertEquals(next, remoteConfig.fetch())
            verify(broadcaster, times(1)).broadcast(any())
            assertEquals(next, remoteConfig.latest)
        }
}
