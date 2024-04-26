package com.projectcitybuild.repositories

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.filecache.FileCache

data class ScheduledAnnouncements(
    val lastBroadcastIndex: Int,
)

class ScheduledAnnouncementsRepository(
    private val config: Config<PluginConfig>,
    private val fileCache: FileCache<ScheduledAnnouncements>,
) {
    private var lastBroadcastIndex: Int? = null

    fun getNextAnnouncement(): String {
        val lastIndex = lastBroadcastIndex
            ?: fileCache.get()?.lastBroadcastIndex
            ?: -1

        val announcements = config.get().announcements.messages

        val index = (lastIndex + 1) % announcements.size
        lastBroadcastIndex = index
        fileCache.put(
            ScheduledAnnouncements(lastBroadcastIndex = index),
        )

        return announcements[index]
    }
}