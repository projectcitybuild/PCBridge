package com.projectcitybuild.integrations.essentials

import com.earth2me.essentials.Essentials
import com.projectcitybuild.entities.events.PlayerPreWarpEvent
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class EssentialsIntegration(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
) : Listener, SpigotIntegration {

    class EssentialsAPINotFoundException : Exception("Essentials plugin not found")

    private var essentials: Essentials? = null

    override fun onEnable() {
        val anyPlugin = plugin.server.pluginManager.getPlugin("Essentials")
        if (anyPlugin == null) {
            logger.severe("Cannot find EssentialsX plugin. Disabling integration")
            return
        }
        if (anyPlugin !is Essentials) {
            logger.severe("Found EssentialsX plugin but cannot access API")
            throw EssentialsAPINotFoundException()
        }

        logger.info("Essentials integration enabled")

        essentials = anyPlugin
    }

    override fun onDisable() {
        if (essentials != null) {
            essentials = null
        }
    }

    /**
     * Sets the player's current position as their last known location
     * in Essentials
     */
    @EventHandler
    fun onPlayerPreWarp(event: PlayerPreWarpEvent) {
        val essentials = essentials
            ?: return

        essentials
            .getUser(event.player)
            .setLastLocation()

        logger.verbose("Registered last location for ${event.player.name} with Essentials")
    }
}
