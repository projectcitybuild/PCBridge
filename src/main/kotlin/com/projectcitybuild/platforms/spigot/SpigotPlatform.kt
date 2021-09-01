package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.utilities.PlayerStore
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.commands.*
import com.projectcitybuild.platforms.spigot.environment.*
import com.projectcitybuild.platforms.spigot.listeners.*
import com.projectcitybuild.platforms.spigot.extensions.addDefault
import org.bukkit.plugin.java.JavaPlugin

class SpigotPlatform: JavaPlugin() {

    private val spigotLogger = SpigotLogger(logger = this.logger)
    private val spigotConfig = SpigotConfig(config = this.config)
    private val scheduler = SpigotScheduler(plugin = this)
    private val apiClient = APIClient(plugin = this)
    private val playerStore = PlayerStore()
    private var playerStoreWrapper: SpigotPlayerStore? = null
    private var permissionsManager: PermissionsManager? = null
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

    private val syncPlayerGroupAction: SyncPlayerGroupAction by lazy {
        SyncPlayerGroupAction(
                permissionsManager!!,
                apiRequestFactory,
                apiClient,
                spigotConfig,
                spigotLogger
        )
    }

    private val checkBanStatusAction: CheckBanStatusAction by lazy {
        CheckBanStatusAction(apiRequestFactory, apiClient)
    }

    override fun onEnable() {
        createDefaultConfig()

        playerStoreWrapper = SpigotPlayerStore(plugin = this, store = playerStore)
        permissionsManager = PermissionsManager()

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
        permissionsManager = null

        logger.info("PCBridge disabled")
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
        arrayOf(
                BanCommand(scheduler, apiRequestFactory),
                UnbanCommand(scheduler, apiRequestFactory),
                CheckBanCommand(scheduler, apiRequestFactory, apiClient, checkBanStatusAction),
                MuteCommand(playerStore),
                UnmuteCommand(playerStore),
                MaintenanceCommand(),
                SyncCommand(scheduler, permissionsManager!!, apiRequestFactory, apiClient, spigotLogger, syncPlayerGroupAction),
                SyncOtherCommand(syncPlayerGroupAction),
                BoxCommand(scheduler, apiRequestFactory)
        )
        .forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        arrayOf(
                BanConnectionListener(apiRequestFactory),
                ChatListener(spigotConfig, playerStore, permissionsManager!!, spigotLogger),
                MaintenanceConnectListener(spigotConfig),
                SyncRankLoginListener(syncPlayerGroupAction)
        )
        .forEach { listener -> delegate.register(listener) }
    }

    private fun createDefaultConfig() {
        config.addDefault(PluginConfig.SETTINGS.MAINTENANCE_MODE)
        config.addDefault(PluginConfig.API.KEY)
        config.addDefault(PluginConfig.API.BASE_URL)
        config.addDefault(PluginConfig.GROUPS.GUEST)
        config.addDefault(PluginConfig.GROUPS.TRUST_PRIORITY)
        config.addDefault(PluginConfig.GROUPS.BUILD_PRIORITY)
        config.addDefault(PluginConfig.GROUPS.DONOR_PRIORITY)

        config.addDefault("groups.appearance.admin.display_name", "§4[Staff]")
        config.addDefault("groups.appearance.admin.hover_name", "Administrator")
        config.addDefault("groups.appearance.sop.display_name", "§c[Staff]")
        config.addDefault("groups.appearance.sop.hover_name", "Senior Operator")
        config.addDefault("groups.appearance.op.display_name", "§6[Staff]")
        config.addDefault("groups.appearance.op.hover_name", "Operator")
        config.addDefault("groups.appearance.moderator.display_name", "§e[Staff]")
        config.addDefault("groups.appearance.moderator.hover_name", "Moderator")

        config.addDefault("groups.appearance.trusted+.hover_name", "Trusted+")
        config.addDefault("groups.appearance.trusted.hover_name", "Trusted")
        config.addDefault("groups.appearance.donator.hover_name", "Donor")
        config.addDefault("groups.appearance.architect.hover_name", "Architect")
        config.addDefault("groups.appearance.engineer.hover_name", "Engineer")
        config.addDefault("groups.appearance.planner.hover_name", "Planner")
        config.addDefault("groups.appearance.builder.hover_name", "Builder")
        config.addDefault("groups.appearance.intern.hover_name", "Intern")

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
