package com.projectcitybuild.integrations

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.support.spigot.SpigotIntegration
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    private val sentry: SentryReporter,
    private val logger: PlatformLogger,
) : Listener, SpigotIntegration {

    private var essentials: Essentials? = null

    override suspend fun onEnable() = runCatching {
        val anyPlugin = plugin.server.pluginManager.getPlugin("Essentials")
        if (anyPlugin == null) {
            logger.severe("Cannot find EssentialsX plugin. Disabling integration")
            return
        }
        check (anyPlugin is Essentials) {
            logger.severe("Found Essentials plugin but cannot access API")
            "Cannot cast plugin to Essentials"
        }

        logger.info("Essentials integration enabled")
        essentials = anyPlugin

        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
    }.onFailure {
        logger.severe("Failed to enable Essentials integration: ${it.localizedMessage}")
        sentry.report(it)
    }.let {}

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
