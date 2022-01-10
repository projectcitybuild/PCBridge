package com.projectcitybuild.platforms.spigot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.environment.*
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.modules.config.implementations.SpigotConfig
import com.projectcitybuild.modules.logger.implementations.SpigotLogger
import com.projectcitybuild.modules.permissions.PermissionsManager
import com.projectcitybuild.modules.scheduler.implementations.SpigotScheduler
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
    private var commandRegistry: SpigotCommandRegistry? = null
    private var listenerRegistry: SpigotListenerRegistry? = null

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
        )
        )

        permissionsManager = PermissionsManager()

        commandRegistry = SpigotCommandRegistry(plugin = this, logger = spigotLogger)

        val listenerRegistry = SpigotListenerRegistry(plugin = this, logger = spigotLogger)
        registerListeners(delegate = listenerRegistry)
        this.listenerRegistry = listenerRegistry

        arrayOf(
            ChatModule.Spigot(plugin = this),
            HubModule.Spigot(plugin = this),
            WarpModule.Spigot(plugin = this),
        ).forEach { module ->
            module.spigotCommands.forEach { commandRegistry?.register(it) }
            module.spigotListeners.forEach { listenerRegistry?.register(it) }
        }

        logger.info("PCBridge ready")
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)

        sessionCache = null

        listenerRegistry?.unregisterAll()

        commandRegistry = null
        listenerRegistry = null
        permissionsManager = null

        logger.info("PCBridge disabled")
    }

    private fun registerListeners(delegate: SpigotListenerRegistry) {
        arrayOf(
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
