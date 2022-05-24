package com.projectcitybuild.plugin.integrations.essentials

import com.earth2me.essentials.Essentials
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.plugin.SpigotIntegration
import com.projectcitybuild.plugin.events.PlayerPreWarpEvent
import dagger.Reusable
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import javax.inject.Inject

@Reusable
class EssentialsIntegration @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
) : SpigotListener, SpigotIntegration {

    class EssentialsAPINotFoundException : Exception("Essentials plugin not found")

    private var essentials: Essentials? = null

    override fun onEnable() {
        val anyPlugin = plugin.server.pluginManager.getPlugin("Essentials")
        if (anyPlugin == null) {
            logger.fatal("Cannot find EssentialsX plugin. Disabling integration")
            return
        }
        if (anyPlugin !is Essentials) {
            logger.fatal("Found EssentialsX plugin but cannot access API")
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
