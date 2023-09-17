package com.projectcitybuild.repositories

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.mock
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.filecache.FileCache
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

class ScheduledAnnouncementsRepositoryTest {
    private lateinit var config: Config<ConfigData>
    private lateinit var fileCache: FileCache<ScheduledAnnouncements>
    private lateinit var repository: ScheduledAnnouncementsRepository

    @BeforeEach
    fun setUp() {
        config = mock<Config<ConfigData>>()
        fileCache = mock<FileCache<ScheduledAnnouncements>>()

        repository = ScheduledAnnouncementsRepository(
            config,
            fileCache,
        )

        whenever(config.get()).thenReturn(ConfigData.default.copy(
            announcements = ConfigData.Announcements(
                intervalInMins = 5,
                messages = listOf(
                    "message1",
                    "message2",
                    "message3",
                )
            )
        ))
    }

    @Test
    fun `returns messages from config in a loop`() = runTest {
        whenever(fileCache.get()).thenReturn(null)

        assertEquals("message1", repository.getNextAnnouncement())
        assertEquals("message2", repository.getNextAnnouncement())
        assertEquals("message3", repository.getNextAnnouncement())
        assertEquals("message1", repository.getNextAnnouncement())
    }

    @Test
    fun `caches last broadcast message index`() = runTest {
        whenever(fileCache.get()).thenReturn(null)

        repository.getNextAnnouncement()
        repository.getNextAnnouncement()
        repository.getNextAnnouncement()
        repository.getNextAnnouncement()

        val order = inOrder(fileCache)
        order.verify(fileCache).put(
            ScheduledAnnouncements(lastBroadcastIndex = 0)
        )
        order.verify(fileCache).put(
            ScheduledAnnouncements(lastBroadcastIndex = 1)
        )
        order.verify(fileCache).put(
            ScheduledAnnouncements(lastBroadcastIndex = 2)
        )
        order.verify(fileCache).put(
            ScheduledAnnouncements(lastBroadcastIndex = 0)
        )
    }

    @Test
    fun `starts from last broadcast index if possible`() = runTest {
        whenever(fileCache.get()).thenReturn(
            ScheduledAnnouncements(lastBroadcastIndex = 1)
        )
        assertEquals("message3", repository.getNextAnnouncement())
        assertEquals("message1", repository.getNextAnnouncement())
    }
}