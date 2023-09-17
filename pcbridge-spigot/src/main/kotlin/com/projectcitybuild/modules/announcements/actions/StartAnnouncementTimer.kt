package com.projectcitybuild.modules.announcements.actions

import com.projectcitybuild.ConfigData
import com.projectcitybuild.pcbridge.core.contracts.PlatformTimer
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.repositories.ScheduledAnnouncementsRepository
import com.projectcitybuild.support.spigot.SpigotServer
import net.md_5.bungee.api.chat.TextComponent
import java.util.concurrent.TimeUnit

class StartAnnouncementTimer(
    private val repository: ScheduledAnnouncementsRepository,
    private val config: Config<ConfigData>,
    private val timer: PlatformTimer,
    private val server: SpigotServer,
) {
    private val timerId = "scheduled_announcements"

    fun start() {
        val config = config.get()
        val intervalInMins = config.announcements.intervalInMins

        timer.cancel(timerId)
        timer.scheduleRepeating(
            identifier = timerId,
            delay = 0,
            repeatingInterval = 5,
            // repeatingInterval = intervalInMins.toLong(),
            unit = TimeUnit.SECONDS,
            work = {
                val message = repository.getNextAnnouncement()
                server.broadcastMessage(
                    TextComponent(message)
                )
            }
        )
    }

    fun stop() {
        timer.cancel(timerId)
    }
}