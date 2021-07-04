package com.projectcitybuild.platforms.spigot

import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPlatform: JavaPlugin() {

    private var commandDelegate: SpigotCommandDelegate? = null
    private var listenerDelegate: SpigotListenerDelegate? = null

    private val weakRef: WeakReference<JavaPlugin> = WeakReference(this)

    override fun onEnable() {
        createDefaultConfig()

//        val commandDelegate = SpigotCommandDelegate(plugin = weakRef, environment = environment)
//        registerCommands(delegate = commandDelegate)
//        this.commandDelegate = commandDelegate
//
//        val listenerDelegate = SpigotListenerDelegate(plugin = weakRef, environment = environment)
//        registerListeners(delegate = listenerDelegate)
//        this.listenerDelegate = listenerDelegate
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
    }

    private fun createDefaultConfig() {
//        val plugin = weakRef.get() ?: throw Exception("Plugin reference lost")
//
//        plugin.config.addDefault<PluginConfig.Settings.MAINTENANCE_MODE>()
//        plugin.config.addDefault<PluginConfig.API.KEY>()
//        plugin.config.addDefault<PluginConfig.API.BASE_URL>()
//
//        plugin.config.options().copyDefaults(true)
//        plugin.saveConfig()
    }
}
