package com.projectcitybuild.pcbridge.paper.features.announcements.actions

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

class StartAnnouncementTimer(
    private val repository: AnnouncementRepository,
    private val remoteConfig: RemoteConfig,
    private val timer: SpigotTimer,
    private val server: Server,
) {
    private val timerId = "scheduled_announcements"

    fun start() {
        val config = remoteConfig.latest.config
        val interval = config.announcements.intervalInMins.minutes

        if (config.announcements.messages.isEmpty()) return

        timer.cancel(timerId)
        timer.scheduleRepeating(
            identifier = timerId,
            delay = interval, // No point doing an announcement just as the server starts
            repeatingInterval = interval,
            work = {
                val message = runBlocking { repository.getNextAnnouncement() }
                server.broadcast(
                    MiniMessage.miniMessage().deserialize(message),
                )
            },
        )
    }

    fun stop() {
        timer.cancel(timerId)
    }
}
