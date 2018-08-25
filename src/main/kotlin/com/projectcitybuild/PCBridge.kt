package com.projectcitybuild

import com.projectcitybuild.core.protocols.Controller
import com.projectcitybuild.core.protocols.Listenable
import com.projectcitybuild.entities.models.PluginConfig
import com.projectcitybuild.spigot.CommandDelegator
import com.projectcitybuild.spigot.ListenerDelegator
import com.projectcitybuild.spigot.environment.SpigotEnvironment
import com.projectcitybuild.spigot.modules.bans.BanController
import com.projectcitybuild.spigot.stores.SpigotPlayerStore
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class PCBridge : JavaPlugin() {

    private var commandDelegate: CommandDelegator? = null
    private var listenerDelegate: ListenerDelegator? = null

    override fun onEnable() {
        super.onEnable()

        val playerStore = SpigotPlayerStore(plugin = WeakReference(this))
        val environment = SpigotEnvironment(logger = logger, playerStore = playerStore.store)

        commandDelegate = CommandDelegator(plugin = WeakReference(this), environment = environment)
        listenerDelegate = ListenerDelegator(plugin = WeakReference(this), environment = environment)

        createDefaultConfig()

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
        config.addDefault(PluginConfig.Settings.MAINTENANCE_MODE().key, false)
        config.addDefault(PluginConfig.Api.KEY().key, "")
        config.addDefault(PluginConfig.Api.BASE_URL().key, "https://projectcitybuild.com/api")

        config.options().copyDefaults(true)
        saveConfig()
    }

}