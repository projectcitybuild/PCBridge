package com.projectcitybuild

import com.projectcitybuild.core.api.APIProvider
import com.projectcitybuild.core.api.client.MojangClient
import com.projectcitybuild.core.api.client.PCBClient
import com.projectcitybuild.core.contracts.*
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.platforms.spigot.SpigotCommandDelegate
import com.projectcitybuild.platforms.spigot.SpigotListenerDelegate
import com.projectcitybuild.platforms.spigot.environment.SpigotEnvironment
import com.projectcitybuild.platforms.spigot.environment.SpigotPlayerStore
import com.projectcitybuild.platforms.spigot.environment.SpigotPluginHook
import com.projectcitybuild.platforms.spigot.extensions.addDefault
import com.projectcitybuild.platforms.spigot.commands.*
import com.projectcitybuild.platforms.spigot.listeners.*
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPlatform(plugin: JavaPlugin): PlatformBridgable {

    private var commandDelegate: CommandDelegatable? = null
    private var listenerDelegate: ListenerDelegatable? = null

    private var _apiProvider: APIProvider? = null
    private val apiProvider: APIProvider
        get() {
            if (_apiProvider == null) {
                _apiProvider = createAPIProvider()
            }
            return _apiProvider!!
        }

    private val weakRef = WeakReference(plugin)

    override val environment: EnvironmentProvider = SpigotEnvironment(
            pluginRef = weakRef,
            logger = plugin.logger,
            playerStore = SpigotPlayerStore(plugin = weakRef).store,
            config = plugin.config,
            hooks = SpigotPluginHook()
    )

    override fun onEnable() {
        createDefaultConfig()

        val commandDelegate = SpigotCommandDelegate(plugin = weakRef, environment = environment)
        registerCommands(delegate = commandDelegate)
        this.commandDelegate = commandDelegate

        val listenerDelegate = SpigotListenerDelegate(plugin = weakRef, environment = environment)
        registerListeners(delegate = listenerDelegate)
        this.listenerDelegate = listenerDelegate
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
        arrayOf(
                BanCommand(environment, apiProvider),
                UnbanCommand(environment, apiProvider),
                CheckBanCommand(environment, apiProvider),
                MuteCommand(environment),
                UnmuteCommand(environment),
                MaintenanceCommand(environment),
                SyncCommand(environment, apiProvider)
        )
        .forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        arrayOf(
                BanConnectionListener(environment, apiProvider),
                ChatListener(environment),
                MaintenanceConnectListener(environment),
                SyncRankLoginListener(environment, apiProvider)
        )
        .forEach { listener -> delegate.register(listener) }
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
        val isLoggingEnabled = environment.get(PluginConfig.API.IS_LOGGING_ENABLED()) as? Boolean
                ?: throw Exception("Could not cast is_logging_enabled to Boolean")

        val pcbClient = PCBClient(
                authToken = environment.get(PluginConfig.API.KEY()) as? String
                        ?: throw Exception("Could not cast auth token to String"),
                baseUrl = environment.get(PluginConfig.API.BASE_URL()) as? String
                        ?: throw Exception("Could not cast base url to String"),
                withLogging = isLoggingEnabled
        )
        val mojangClient = MojangClient(
                withLogging = isLoggingEnabled
        )
        return APIProvider(pcb = pcbClient, mojang = mojangClient)
    }
}
