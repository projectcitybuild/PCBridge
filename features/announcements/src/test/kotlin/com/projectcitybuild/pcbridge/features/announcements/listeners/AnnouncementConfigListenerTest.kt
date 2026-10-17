package com.projectcitybuild.pcbridge.features.announcements.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class AnnouncementConfigListenerTest {
    private val timer = mock(StartAnnouncementTimer::class.java)
    private val listener = AnnouncementConfigListener(timer)

    @Test
    fun `restarts the timer when announcement settings change`() {
        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(config(1, "old"), config(2, "new")),
        )

        verify(timer).stop()
        verify(timer).start()
    }

    @Test
    fun `does not restart the timer when announcement settings are unchanged`() {
        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(config(1, "same"), config(2, "same")),
        )

        verify(timer, never()).stop()
        verify(timer, never()).start()
    }

    private fun config(
        version: Int,
        message: String,
    ) = RemoteConfigVersion(
        version,
        RemoteConfigKeyValues(
            announcements = RemoteConfigKeyValues.Announcements(messages = listOf(message)),
        ),
    )
}
