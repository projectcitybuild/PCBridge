package com.projectcitybuild.features.cosmetics.adapters.gadgetsmenu

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.logger.PlatformLogger
import com.yapzhenyie.GadgetsMenu.economy.GEconomyProvider
import com.yapzhenyie.GadgetsMenu.player.OfflinePlayerManager
import dagger.Reusable
import org.bukkit.plugin.Plugin
import javax.inject.Inject

@Reusable
class GadgetsMenuAdapter @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
): SpigotListener {
    private var isEnabled = false

    fun enable() {
        if (plugin.server.pluginManager.isPluginEnabled("GadgetsMenu")) {
            GEconomyProvider.setMysteryDustStorage(CurrencyProvider(plugin, logger))
        } else {
            logger.warning("Cannot find GadgetsMenu plugin. Disabling integration")
            return
        }
        isEnabled = true
    }

    class CurrencyProvider constructor(
        plugin: Plugin,
        private val logger: PlatformLogger,
    ): GEconomyProvider(plugin, "pcbridge") {

        override fun getMysteryDust(p0: OfflinePlayerManager?): Int {
            logger.info("[GADGETSMENU] getMysteryDust: $p0")
            return 1000
        }

        override fun addMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            logger.info("[GADGETSMENU] addMysteryDust: $p0")
            return true
        }

        override fun setMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            logger.info("[GADGETSMENU] setMysteryDust: $p0")
            return true
        }

        override fun removeMysteryDust(p0: OfflinePlayerManager?, p1: Int): Boolean {
            logger.info("[GADGETSMENU] removeMysteryDust: $p0")
            return true
        }
    }
}