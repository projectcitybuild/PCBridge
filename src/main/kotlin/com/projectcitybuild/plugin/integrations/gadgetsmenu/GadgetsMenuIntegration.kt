package com.projectcitybuild.plugin.integrations.gadgetsmenu

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.plugin.integrations.SpigotIntegration
import com.projectcitybuild.repositories.CurrencyRepository
import com.projectcitybuild.support.spigot.logger.PlatformLogger
import com.yapzhenyie.GadgetsMenu.economy.GEconomyProvider
import com.yapzhenyie.GadgetsMenu.player.OfflinePlayerManager
import dagger.Reusable
import org.bukkit.plugin.Plugin
import javax.inject.Inject

@Reusable
class GadgetsMenuIntegration @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
    private val currencyRepository: CurrencyRepository,
) : SpigotListener, SpigotIntegration {

    private var isEnabled = false

    override fun onEnable() {
        if (plugin.server.pluginManager.isPluginEnabled("GadgetsMenu")) {
            GEconomyProvider.setMysteryDustStorage(
                CurrencyProvider(plugin, logger, currencyRepository)
            )
        } else {
            logger.warning("Cannot find GadgetsMenu plugin. Disabling integration")
            return
        }
        logger.info("GadgetsMenu integration enabled")

        isEnabled = true
    }

    override fun onDisable() {
        isEnabled = false
    }

    /**
     * Hooks into GadgetsMenu's economy API to point at our own
     * player currency repository.
     *
     * Note: all functions are called by GadgetsMenu on a background thread
     */
    class CurrencyProvider constructor(
        plugin: Plugin,
        private val logger: PlatformLogger,
        private val repository: CurrencyRepository,
    ) : GEconomyProvider(plugin, "pcbridge") {

        override fun getMysteryDust(p0: OfflinePlayerManager?): Int {
            if (p0 == null) {
                logger.warning("Attempted to call getMysteryDust with a null OfflinePlayerManager")
                return 0
            }
            return repository.getBalance(playerUUID = p0.uuid)
        }

        override fun setMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            // Not permitted
            return false
        }

        override fun addMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            // Not permitted
            return false
        }

        override fun removeMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            if (p0 == null) {
                logger.warning("Attempted to call removeMysteryDust with a null OfflinePlayerManager")
                return false
            }
            return repository.deduct(
                playerUUID = p0.uuid,
                amount = p1,
                reason = "Minecraft cosmetic purchase"
            )
        }
    }
}
