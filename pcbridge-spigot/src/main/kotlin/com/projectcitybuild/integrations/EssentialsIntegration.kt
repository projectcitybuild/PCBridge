package com.projectcitybuild.integrations

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.support.PlatformLogger
import com.projectcitybuild.support.spigot.SpigotIntegration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    private val sentry: SentryReporter,
    private val logger: PlatformLogger,
) : Listener, SpigotIntegration(
    pluginName = "Essentials",
    pluginManager = plugin.server.pluginManager,
    logger = logger,
    sentry = sentry,
) {
    private var essentials: Essentials? = null

    override suspend fun onEnable(loadedPlugin: Plugin) {
        check (loadedPlugin is Essentials) {
            "Found Essentials plugin but cannot cast to Essentials class"
        }
        essentials = loadedPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        logger.info("Essentials integration enabled")
    }

    override suspend fun onDisable() {
        if (essentials != null) {
            PlayerPreWarpEvent.getHandlerList().unregister(this)
            essentials = null
        }
    }

    /**
     * Sets the player's current position as their last known location
     * in Essentials
     */
    @EventHandler
    fun onPlayerPreWarp(event: PlayerPreWarpEvent) = runCatching {
        if (essentials == null) {
            logger.warning("Essentials integration disabled but it's still listening to events")
            return@runCatching
        }
        essentials!!
            .getUser(event.player)
            .setLastLocation()

        logger.verbose("Registered last location for ${event.player.name} with Essentials")
    }
}
