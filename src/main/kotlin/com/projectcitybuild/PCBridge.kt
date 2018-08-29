package com.projectcitybuild

import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.extensions.addDefault
import com.projectcitybuild.entities.models.PluginConfig
import com.projectcitybuild.spigot.CommandDelegate
import com.projectcitybuild.spigot.ListenerDelegate
import com.projectcitybuild.spigot.environment.SpigotEnvironment
import com.projectcitybuild.spigot.modules.bans.BanController
import com.projectcitybuild.spigot.modules.chat.ChatController
import com.projectcitybuild.spigot.modules.maintenance.MaintenanceController
import com.projectcitybuild.spigot.stores.SpigotPlayerStore
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference
import java.util.logging.Level

class PCBridge : JavaPlugin() {

    private var commandDelegate: CommandDelegate? = null
    private var listenerDelegate: ListenerDelegate? = null

    override fun onEnable() {
        super.onEnable()

        logger.level = Level.ALL

        logger.info("""
            ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
            ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
            ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗
            ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝
            ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
            ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
            https://projectcitybuild.com
        """)

        createDefaultConfig()

        val playerStore = SpigotPlayerStore(plugin = WeakReference(this))
        val environment = SpigotEnvironment(logger = logger, playerStore = playerStore.store, config = config)

        commandDelegate = CommandDelegate(plugin = WeakReference(this), environment = environment)
        listenerDelegate = ListenerDelegate(plugin = WeakReference(this), environment = environment)

        this.register(modules = arrayOf(
                BanController(),
                ChatController(),
                MaintenanceController()
        ))

        logger.info("PCBridge ready")
    }

    override fun onDisable() {
        super.onDisable()

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
        config.addDefault<PluginConfig.Settings.MAINTENANCE_MODE>()
        config.addDefault<PluginConfig.Api.KEY>()
        config.addDefault<PluginConfig.Api.BASE_URL>()

        config.options().copyDefaults(true)
        saveConfig()
    }

}