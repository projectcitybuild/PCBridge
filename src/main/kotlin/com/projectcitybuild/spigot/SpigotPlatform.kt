package com.projectcitybuild

import com.projectcitybuild.core.contracts.*
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.spigot.SpigotCommandDelegate
import com.projectcitybuild.spigot.SpigotEventRegistry
import com.projectcitybuild.spigot.SpigotListenerDelegate
import com.projectcitybuild.spigot.environment.SpigotEnvironment
import com.projectcitybuild.spigot.environment.SpigotPlayerStore
import com.projectcitybuild.spigot.environment.SpigotPluginHook
import com.projectcitybuild.spigot.extensions.addDefault
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference
import java.util.logging.Logger

class SpigotPlatform (
        private val plugin: WeakReference<JavaPlugin>,
        private val config: FileConfiguration,
        private val logger: Logger
): PlatformBridgable {

    private var commandDelegate: CommandDelegatable? = null
    private var listenerDelegate: ListenerDelegatable? = null

    override val environment: EnvironmentProvider = SpigotEnvironment(
            pluginRef = plugin,
            logger = logger,
            playerStore = SpigotPlayerStore(plugin = plugin).store,
            config = config,
            hooks = SpigotPluginHook(plugin = plugin)
    )

    override fun onEnable() {
        createDefaultConfig()

        commandDelegate = SpigotCommandDelegate(plugin = plugin, environment = environment)
        listenerDelegate = SpigotListenerDelegate(plugin = plugin, environment = environment)

        this.register(modules = arrayOf(
                SpigotEventRegistry()
        ))
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null

        logger.info("PCBridge disabled")
    }

    private fun register(modules: Array<Controller>) {
        modules.forEach { controller ->
            controller.commands.forEach { command ->
                commandDelegate?.register(command)
            }
            controller.listeners.forEach { listener ->
                listenerDelegate?.register(listener)
            }
        }
    }

    private fun createDefaultConfig() {
        val plugin = plugin.get() ?: throw Exception("Plugin reference lost")

        plugin.config.addDefault<PluginConfig.Settings.MAINTENANCE_MODE>()
        plugin.config.addDefault<PluginConfig.API.KEY>()
        plugin.config.addDefault<PluginConfig.API.BASE_URL>()

        config.options().copyDefaults(true)
        plugin.saveConfig()
    }
}
