package com.projectcitybuild

import com.projectcitybuild.core.contracts.PlatformBridgable
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

class PCBridge: JavaPlugin {

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

    /* Required for MockBukkit to work */
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File)
            : super(loader, description, dataFolder, file)
}