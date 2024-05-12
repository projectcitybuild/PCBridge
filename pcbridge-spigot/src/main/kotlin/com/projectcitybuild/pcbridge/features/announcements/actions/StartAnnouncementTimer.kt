package com.projectcitybuild.pcbridge.features.announcements.actions

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.support.spigot.SpigotTimer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import java.util.concurrent.TimeUnit

class StartAnnouncementTimer(
    private val repository: AnnouncementRepository,
    private val config: Config,
    private val timer: SpigotTimer,
    private val server: Server,
) {
    private val timerId = "scheduled_announcements"

    suspend fun start() {
        val config = config.get()
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
