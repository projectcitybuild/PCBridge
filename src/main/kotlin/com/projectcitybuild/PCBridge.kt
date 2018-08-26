package com.projectcitybuild

import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.core.extensions.addDefault
import com.projectcitybuild.entities.models.PluginConfig
import com.projectcitybuild.spigot.CommandDelegate
import com.projectcitybuild.spigot.ListenerDelegate
import com.projectcitybuild.spigot.environment.SpigotEnvironment
import com.projectcitybuild.spigot.modules.bans.BanController
import com.projectcitybuild.spigot.stores.SpigotPlayerStore
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class PCBridge : JavaPlugin() {

    private var commandDelegate: CommandDelegate? = null
    private var listenerDelegate: ListenerDelegate? = null

    override fun onEnable() {
        super.onEnable()

        createDefaultConfig()

        val playerStore = SpigotPlayerStore(plugin = WeakReference(this))
        val environment = SpigotEnvironment(logger = logger, playerStore = playerStore.store, config = config)

        commandDelegate = CommandDelegate(plugin = WeakReference(this), environment = environment)
        listenerDelegate = ListenerDelegate(plugin = WeakReference(this), environment = environment)

        this.register(modules = arrayOf(
                BanController()
        ))

        this.logger.info("PCBridge enabled")
    }

    override fun onDisable() {
        super.onDisable()

        commandDelegate = null
        listenerDelegate = null

        this.logger.info("PCBridge disabled")
    }

    private fun register(modules: Array<Controller>) {
        modules.forEach { controller ->
            controller.commands.forEach { command ->
                commandDelegate?.register(command)
            }
            controller.listeners.forEach { listener: Listenable<Event> ->
                listenerDelegate?.register(listener)
            }
        }
    }

    private fun createDefaultConfig() {
        config.addDefault<PluginConfig.Settings.MAINTENANCE_MODE>()
        config.addDefault<PluginConfig.Api.KEY>()
        config.addDefault<PluginConfig.Api.BASE_URL>()

        config.options().copyDefaults(true)
        saveConfig()
    }

}