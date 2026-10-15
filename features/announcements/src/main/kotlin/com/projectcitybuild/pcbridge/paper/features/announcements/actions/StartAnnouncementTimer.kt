package com.projectcitybuild.pcbridge.paper.features.announcements.actions

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfig
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.minutes

class StartAnnouncementTimer(
    private val plugin: Plugin,
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
            // No point doing an announcement just as the server starts.
            delay = interval,
            repeatingInterval = interval,
            work = {
                val message = repository.getNextAnnouncement()
                if (message.isNotEmpty()) {
                    val deserializedMessage = MiniMessage.miniMessage().deserialize(message)

                    withContext(plugin.minecraftDispatcher) {
                        server.broadcast(deserializedMessage)
                    }
                }
            },
        )
    }

    fun stop() {
        timer.cancel(timerId)
    }
}
