package com.projectcitybuild

import com.projectcitybuild.core.contracts.PlatformBridgable
import org.bukkit.plugin.java.JavaPlugin

class PCBridge: JavaPlugin() {

    private var _platformBridge: PlatformBridgable? = null
    private val platformBridge: PlatformBridgable
        get() {
            if (_platformBridge == null) {
                _platformBridge = SpigotPlatform(plugin = this)
            }
            return _platformBridge!!
        }

    override fun onEnable() {
        super.onEnable()

        platformBridge.onEnable()

        logger.info("PCBridge ready")
    }

    override fun onDisable() {
        super.onDisable()

        platformBridge.onDisable()

        logger.info("PCBridge disabled")
    }
}