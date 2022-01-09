package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.commands.SetHubCommand
import com.projectcitybuild.platforms.spigot.commands.SetWarpCommand
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import com.projectcitybuild.platforms.spigot.environment.SpigotConfig
import com.projectcitybuild.platforms.spigot.environment.SpigotLogger
import com.projectcitybuild.platforms.spigot.environment.SpigotScheduler
import com.projectcitybuild.platforms.spigot.listeners.AFKListener
import com.projectcitybuild.platforms.spigot.listeners.ChatListener
import com.projectcitybuild.platforms.spigot.listeners.IncomingPluginMessageListener
import com.projectcitybuild.platforms.spigot.listeners.PendingJoinActionListener
import org.bukkit.plugin.java.JavaPlugin

class SpigotPlatform: JavaPlugin() {

    private val spigotLogger = SpigotLogger(logger = this.logger)
    private val spigotConfig = SpigotConfig(config = this.config)
    private val scheduler = SpigotScheduler(plugin = this)
    private val apiClient = APIClient(getCoroutineContext = {
        // To prevent Coroutines being created before the plugin is ready
        this.minecraftDispatcher
    })
    private var permissionsManager: PermissionsManager? = null
    private var commandDelegate: SpigotCommandDelegate? = null
    private var listenerDelegate: SpigotListenerDelegate? = null

    private val apiRequestFactory: APIRequestFactory by lazy {
        val isLoggingEnabled = spigotConfig.get(PluginConfig.API_IS_LOGGING_ENABLED)

        APIRequestFactory(
            pcb = PCBClient(
                authToken = spigotConfig.get(PluginConfig.API_KEY),
                baseUrl = spigotConfig.get(PluginConfig.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }

    private var sessionCache: SessionCache? = null

    override fun onEnable() {
        createDefaultConfig()

        sessionCache = SessionCache()

        server.messenger.registerOutgoingPluginChannel(this, Channel.BUNGEECORD)
        server.messenger.registerIncomingPluginChannel(this, Channel.BUNGEECORD, IncomingPluginMessageListener(
            plugin = this,
            sessionCache = sessionCache!!,
            logger = spigotLogger,
        ))

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
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)

        sessionCache = null

        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null
        permissionsManager = null

        logger.info("PCBridge disabled")
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
        arrayOf(
            SetHubCommand(plugin = this),
            SetWarpCommand(plugin = this),
        )
        .forEach { command -> delegate.register(command) }
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        arrayOf(
            ChatListener(plugin = this),
            AFKListener(plugin = this),
            PendingJoinActionListener(this, sessionCache!!, spigotLogger),
        )
        .forEach { listener -> delegate.register(listener) }
    }

    private fun createDefaultConfig() {
//        config.addDefault(PluginConfig.API.KEY)
//        config.addDefault(PluginConfig.API.BASE_URL)
//        config.addDefault(PluginConfig.GROUPS.GUEST)
//        config.addDefault(PluginConfig.GROUPS.TRUST_PRIORITY)
//        config.addDefault(PluginConfig.GROUPS.BUILD_PRIORITY)
//        config.addDefault(PluginConfig.GROUPS.DONOR_PRIORITY)

        config.options().copyDefaults(true)
        saveConfig()
    }
}
