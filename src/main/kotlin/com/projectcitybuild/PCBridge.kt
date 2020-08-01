package com.projectcitybuild

import com.projectcitybuild.core.contracts.PlatformBridgable
import org.bukkit.plugin.java.JavaPlugin
import java.lang.Exception

class PCBridge: JavaPlugin() {

    private enum class Platform {
        SPIGOT,
        BUNGEECORD,
    }

    private val platform: Platform = Platform.SPIGOT // TODO: remove hardcoding if we support BungeeCord
    private val platformBridge: PlatformBridgable

    init {
        when (platform) {
            Platform.SPIGOT -> this.platformBridge = SpigotPlatform(plugin = this)

            Platform.BUNGEECORD -> throw Exception("Platform bridging not implemented")
        }
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