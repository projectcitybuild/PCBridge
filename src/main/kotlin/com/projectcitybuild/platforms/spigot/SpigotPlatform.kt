package com.projectcitybuild

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.utilities.PlayerStore
import com.projectcitybuild.platforms.spigot.SpigotCommandDelegate
import com.projectcitybuild.platforms.spigot.SpigotListenerDelegate
import com.projectcitybuild.platforms.spigot.commands.*
import com.projectcitybuild.platforms.spigot.environment.*
import com.projectcitybuild.platforms.spigot.listeners.*
import com.projectcitybuild.platforms.spigot.extensions.addDefault
import org.bukkit.plugin.java.JavaPlugin

class SpigotPlatform: JavaPlugin() {

    private val spigotLogger = SpigotLogger(logger = this.logger)
    private val spigotConfig = SpigotConfig(config = this.config)
    private val scheduler = SpigotScheduler(plugin = this)
    private val apiClient = APIClient(spigotLogger, scheduler)
    private val playerStore = PlayerStore()
    private var playerStoreWrapper: SpigotPlayerStore? = null
    private var permissionsManager = PermissionsManager()
    private var commandDelegate: SpigotCommandDelegate? = null
    private var listenerDelegate: SpigotListenerDelegate? = null

    private var _apiRequestFactory: APIRequestFactory? = null
    private val apiRequestFactory: APIRequestFactory
        get() {
            if (_apiRequestFactory == null) {
                _apiRequestFactory = createAPIProvider()
            }
            return _apiRequestFactory!!
        }

    override fun onEnable() {
        createDefaultConfig()

        playerStoreWrapper = SpigotPlayerStore(plugin = this, store = playerStore)

        val commandDelegate = SpigotCommandDelegate(plugin = this, logger = spigotLogger)
        registerCommands(delegate = commandDelegate)
        this.commandDelegate = commandDelegate

        val listenerDelegate = SpigotListenerDelegate(plugin = this, logger = spigotLogger)
        registerListeners(delegate = listenerDelegate)
        this.listenerDelegate = listenerDelegate

        logger.info("PCBridge ready")
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null

        logger.info("PCBridge disabled")
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
        arrayOf(
                BanCommand(scheduler, apiRequestFactory),
                UnbanCommand(scheduler, apiRequestFactory),
                CheckBanCommand(scheduler, apiRequestFactory),
                MuteCommand(playerStore),
                UnmuteCommand(playerStore),
                MaintenanceCommand(),
                SyncCommand(scheduler, permissionsManager, apiRequestFactory, apiClient, spigotLogger),
                BoxCommand(scheduler, apiRequestFactory)
        )
        .forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        arrayOf(
                BanConnectionListener(apiRequestFactory),
                ChatListener(playerStore, permissionsManager, spigotLogger),
                MaintenanceConnectListener(spigotConfig),
                SyncRankLoginListener(scheduler, permissionsManager, apiRequestFactory, apiClient, spigotLogger)
        )
        .forEach { listener -> delegate.register(listener) }
    }

    private fun createDefaultConfig() {
        config.addDefault(PluginConfig.SETTINGS.MAINTENANCE_MODE)
        config.addDefault(PluginConfig.API.KEY)
        config.addDefault(PluginConfig.API.BASE_URL)

        config.options().copyDefaults(true)
        saveConfig()
    }

    private fun createAPIProvider(): APIRequestFactory {
        val isLoggingEnabled = spigotConfig.get(PluginConfig.API.IS_LOGGING_ENABLED)

        val pcbClient = PCBClient(
                authToken = spigotConfig.get(PluginConfig.API.KEY) as? String
                        ?: throw Exception("Could not cast auth token to String"),
                baseUrl = spigotConfig.get(PluginConfig.API.BASE_URL) as? String
                        ?: throw Exception("Could not cast base url to String"),
                withLogging = isLoggingEnabled
        )
        val mojangClient = MojangClient(
                withLogging = isLoggingEnabled
        )
        return APIRequestFactory(pcb = pcbClient, mojang = mojangClient)
    }
}
