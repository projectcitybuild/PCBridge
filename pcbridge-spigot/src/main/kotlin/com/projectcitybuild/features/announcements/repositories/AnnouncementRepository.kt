package com.projectcitybuild.features.announcements.repositories

import com.projectcitybuild.core.config.Config
import com.projectcitybuild.core.state.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnnouncementRepository(
    private val config: Config,
    private val store: Store,
) {
    suspend fun getNextAnnouncement(): String = withContext(Dispatchers.IO) {
        val announcements = config.get().announcements.messages
        val lastBroadcastIndex = store.state.lastBroadcastIndex
        val nextIndex = (lastBroadcastIndex + 1) % announcements.size
        store.mutate { state ->
            state.copy(lastBroadcastIndex = nextIndex)
        }
        announcements[nextIndex]
    }
}