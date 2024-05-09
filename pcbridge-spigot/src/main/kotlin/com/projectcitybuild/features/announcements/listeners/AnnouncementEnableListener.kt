package com.projectcitybuild.features.announcements.listeners

import com.projectcitybuild.core.config.Config
import com.projectcitybuild.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.support.PlatformLogger
import com.projectcitybuild.support.spigot.SpigotTimer
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class AnnouncementEnableListener(
    private val announcementRepository: AnnouncementRepository,
    private val config: Config,
    private val timer: SpigotTimer,
    private val server: Server,
    private val logger: PlatformLogger,
    private val plugin: JavaPlugin,
): Listener {
    private var action: StartAnnouncementTimer? = null

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin != plugin) {
            // PluginEnableEvent is emitted for every plugin, not just ours
            return
        }
        action = StartAnnouncementTimer(
            announcementRepository,
            config,
            timer,
            server,
        )
        action?.start()

        logger.debug("Announcement timer started")
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin != plugin) {
            // PluginDisableEvent is emitted for every plugin, not just ours
            return
        }
        action?.stop()
        action = null

        logger.debug("Announcement timer stopped")
    }
}