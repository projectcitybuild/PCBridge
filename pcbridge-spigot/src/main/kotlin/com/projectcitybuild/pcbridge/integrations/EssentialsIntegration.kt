package com.projectcitybuild.pcbridge.integrations

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.support.spigot.SpigotIntegration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    sentry: SentryReporter,
) : Listener, SpigotIntegration(
    pluginName = "Essentials",
    pluginManager = plugin.server.pluginManager,
    sentry = sentry,
) {
    private var essentials: Essentials? = null

    override suspend fun onEnable(loadedPlugin: Plugin) {
        check (loadedPlugin is Essentials) {
            "Found Essentials plugin but cannot cast to Essentials class"
        }
        essentials = loadedPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        log.info { "Essentials integration enabled" }
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
            log.warn { "Essentials integration disabled but it's still listening to events" }
            return@runCatching
        }
        essentials!!
            .getUser(event.player)
            .setLastLocation()

        log.debug { "Registered last location for ${event.player.name} with Essentials" }
    }
}
