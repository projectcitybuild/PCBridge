package com.projectcitybuild.modules.announcements.actions

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.mock
import com.projectcitybuild.pcbridge.core.contracts.PlatformTimer
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.repositories.ScheduledAnnouncementsRepository
import com.projectcitybuild.support.spigot.SpigotServer
import net.md_5.bungee.api.chat.TextComponent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class StartAnnouncementTimerTest {
    @Test
    fun `broadcasts messages on a timer`() {
        val repository = mock(ScheduledAnnouncementsRepository::class.java)
        whenever(repository.getNextAnnouncement()).thenReturn(
            "message1",
            "message2",
        )

        val config = mock<Config<ConfigData>>()
        whenever(config.get()).thenReturn(
            ConfigData.default.copy(
                announcements = ConfigData.default.announcements.copy(
                    intervalInMins = 5,
                )
            )
        )

        val timer = mock(PlatformTimer::class.java)
        val server = mock(SpigotServer::class.java)

        val action = StartAnnouncementTimer(
            repository,
            config,
            timer,
            server,
        )
        action.start()

        argumentCaptor<() -> Unit>().apply {
            verify(timer).scheduleRepeating(any(), eq(0), eq(5), eq(TimeUnit.SECONDS), capture())
            firstValue()
            firstValue()
        }

        val order = inOrder(server)
        order.verify(server).broadcastMessage(
            TextComponent("message1")
        )
        order.verify(server).broadcastMessage(
            TextComponent("message2")
        )
    }
}