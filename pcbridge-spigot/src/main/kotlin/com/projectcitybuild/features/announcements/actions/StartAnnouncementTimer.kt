package com.projectcitybuild.features.announcements.actions

import com.projectcitybuild.core.config.Config
import com.projectcitybuild.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.support.spigot.SpigotTimer
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
            delay = 0,
            repeatingInterval = intervalInMins.toLong(),
            unit = TimeUnit.MINUTES,
            work = {
                val message = runBlocking { repository.getNextAnnouncement() }
                server.broadcast(
                    MiniMessage.miniMessage().deserialize(message)
                )
            }
        )
    }

    fun stop() {
        timer.cancel(timerId)
    }
}