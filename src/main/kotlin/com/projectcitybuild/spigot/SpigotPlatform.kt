package com.projectcitybuild

import com.projectcitybuild.api.APIProvider
import com.projectcitybuild.api.client.MojangClient
import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.core.contracts.*
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.spigot.SpigotCommandDelegate
import com.projectcitybuild.spigot.SpigotListenerDelegate
import com.projectcitybuild.spigot.environment.SpigotEnvironment
import com.projectcitybuild.spigot.environment.SpigotPlayerStore
import com.projectcitybuild.spigot.environment.SpigotPluginHook
import com.projectcitybuild.spigot.extensions.addDefault
import com.projectcitybuild.spigot.modules.bans.commands.BanCommand
import com.projectcitybuild.spigot.modules.bans.commands.CheckBanCommand
import com.projectcitybuild.spigot.modules.bans.commands.UnbanCommand
import com.projectcitybuild.spigot.modules.bans.listeners.BanConnectionListener
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand
import com.projectcitybuild.spigot.modules.chat.commands.UnmuteCommand
import com.projectcitybuild.spigot.modules.chat.listeners.ChatListener
import com.projectcitybuild.spigot.modules.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.spigot.modules.maintenance.listeners.MaintenanceConnectListener
import com.projectcitybuild.spigot.modules.ranks.commands.SyncCommand
import com.projectcitybuild.spigot.modules.ranks.listeners.SyncRankLoginListener
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPlatform (plugin: JavaPlugin): PlatformBridgable {

    private var commandDelegate: CommandDelegatable? = null
    private var listenerDelegate: ListenerDelegatable? = null

    private val weakRef = WeakReference(plugin)
    private val apiProvider = createAPIProvider()

    override val environment: EnvironmentProvider = SpigotEnvironment(
            pluginRef = weakRef,
            logger = plugin.logger,
            playerStore = SpigotPlayerStore(plugin = weakRef).store,
            config = plugin.config,
            hooks = SpigotPluginHook(plugin = weakRef)
    )

    override fun onEnable() {
        createDefaultConfig()

        val commandDelegate = SpigotCommandDelegate(plugin = weakRef, environment = environment, apiProvider = apiProvider)
        registerCommands(delegate = commandDelegate)
        this.commandDelegate = commandDelegate

        val listenerDelegate = SpigotListenerDelegate(plugin = weakRef, environment = environment, apiProvider = apiProvider)
        registerListeners(delegate = listenerDelegate)
        this.listenerDelegate = listenerDelegate
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
        val commands = arrayOf(
                BanCommand(),
                UnbanCommand(),
                CheckBanCommand(),
                MuteCommand(),
                UnmuteCommand(),
                MaintenanceCommand(),
                SyncCommand()
        )
        commands.forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        val listeners = arrayOf(
                BanConnectionListener(),
                ChatListener(),
                MaintenanceConnectListener(),
                SyncRankLoginListener()
        )
        listeners.forEach { listener -> delegate.register(listener) }
    }

    private fun createDefaultConfig() {
        val plugin = weakRef.get() ?: throw Exception("Plugin reference lost")

        plugin.config.addDefault<PluginConfig.Settings.MAINTENANCE_MODE>()
        plugin.config.addDefault<PluginConfig.API.KEY>()
        plugin.config.addDefault<PluginConfig.API.BASE_URL>()

        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    private fun createAPIProvider(): APIProvider {
        val pcbClient = PCBClient(
                authToken = environment.get(PluginConfig.API.KEY()) as? String
                        ?: throw Exception("Could not cast auth token to String"),
                baseUrl = environment.get(PluginConfig.API.BASE_URL()) as? String
                        ?: throw Exception("Could not cast base url to String"),
                withLogging = environment.get(PluginConfig.API.IS_LOGGING_ENABLED()) as? Boolean
                        ?: throw Exception("Could not cast is_logging_enabled to Boolean")
        )
        return APIProvider(pcb = pcbClient, mojang = MojangClient())
    }
}
