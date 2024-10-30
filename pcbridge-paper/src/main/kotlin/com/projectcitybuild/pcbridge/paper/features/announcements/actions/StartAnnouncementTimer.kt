package com.projectcitybuild.pcbridge.paper.features.announcements.actions

import com.projectcitybuild.pcbridge.paper.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotTimer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import java.util.concurrent.TimeUnit

class StartAnnouncementTimer(
    private val repository: AnnouncementRepository,
    private val remoteConfig: RemoteConfig,
    private val timer: SpigotTimer,
    private val server: Server,
) {
    private val timerId = "scheduled_announcements"

    fun start() {
        val config = remoteConfig.latest.config
        val intervalInMins = config.announcements.intervalInMins

        timer.cancel(timerId)
        timer.scheduleRepeating(
            identifier = timerId,
            // No point doing an announcement just as the server starts
            delay = intervalInMins.toLong(),
            repeatingInterval = intervalInMins.toLong(),
            unit = TimeUnit.MINUTES,
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
