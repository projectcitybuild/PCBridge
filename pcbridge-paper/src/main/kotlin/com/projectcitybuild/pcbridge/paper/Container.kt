package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.http.discord.DiscordHttp
import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.playerdb.PlayerDbHttp
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PersistedServerState
import com.projectcitybuild.pcbridge.paper.architecture.state.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders.PlayerWorldPlaceholder
import com.projectcitybuild.pcbridge.paper.architecture.webhooks.WebServerDelegate
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.DateTimeFormatter
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.default
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.libs.playerlookup.PlayerLookup
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.storage.TeleportHistoryStorage
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.core.utils.PeriodicRunner
import com.projectcitybuild.pcbridge.paper.features.announcements.announcementsModule
import com.projectcitybuild.pcbridge.paper.features.bans.bansModule
import com.projectcitybuild.pcbridge.paper.features.building.buildingModule
import com.projectcitybuild.pcbridge.paper.features.builds.buildsModule
import com.projectcitybuild.pcbridge.paper.features.chatbadge.chatBadgeModule
import com.projectcitybuild.pcbridge.paper.features.chatemojis.chatEmojisModule
import com.projectcitybuild.pcbridge.paper.features.chaturls.chatUrlsModule
import com.projectcitybuild.pcbridge.paper.features.config.configModule
import com.projectcitybuild.pcbridge.paper.features.groups.groupsModule
import com.projectcitybuild.pcbridge.paper.features.homes.homesModule
import com.projectcitybuild.pcbridge.paper.features.joinmessages.joinMessagesModule
import com.projectcitybuild.pcbridge.paper.features.maintenance.maintenanceModule
import com.projectcitybuild.pcbridge.paper.features.randomteleport.randomTeleportModule
import com.projectcitybuild.pcbridge.paper.features.register.registerModule
import com.projectcitybuild.pcbridge.paper.features.serverlinks.serverLinksModule
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsModule
import com.projectcitybuild.pcbridge.paper.features.staffchat.staffChatModule
import com.projectcitybuild.pcbridge.paper.features.sync.syncModule
import com.projectcitybuild.pcbridge.paper.features.telemetry.telemetryModule
import com.projectcitybuild.pcbridge.paper.features.warnings.warningsModule
import com.projectcitybuild.pcbridge.paper.features.warps.warpsModule
import com.projectcitybuild.pcbridge.paper.features.watchdog.watchDogModule
import com.projectcitybuild.pcbridge.paper.features.workstations.workstationsModule
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.DynmapIntegration
import com.projectcitybuild.pcbridge.paper.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.pcbridge.webserver.HttpServer
import com.projectcitybuild.pcbridge.webserver.data.HttpServerConfig
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

fun pluginModules(plugin: JavaPlugin) = buildList {
    add(mainModule(plugin))
    addAll(featureModules)
}

private fun mainModule(plugin: JavaPlugin) = module {
    spigot(plugin)
    core()
    http()
    webServer()
    integrations()
    architecture()
}

private val featureModules = listOf(
    announcementsModule,
    bansModule,
    buildingModule,
    buildsModule,
    chatBadgeModule,
    chatEmojisModule,
    chatUrlsModule,
    configModule,
    groupsModule,
    homesModule,
    joinMessagesModule,
    maintenanceModule,
    randomTeleportModule,
    registerModule,
    serverLinksModule,
    spawnsModule,
    staffChatModule,
    syncModule,
    telemetryModule,
    warningsModule,
    warpsModule,
    watchDogModule,
    workstationsModule
)

private fun Module.spigot(plugin: JavaPlugin) {
    single { plugin }

    factory { get<JavaPlugin>().server }

    single { SpigotNamespace(plugin = get()) }

    single { SpigotListenerRegistry(plugin = get()) }

    factory { SpigotTimer(plugin = get()) }

    factory {
        SpigotEventBroadcaster(
            server = get(),
            minecraftDispatcher =  { get<JavaPlugin>().minecraftDispatcher },
        )
    }
}

private fun Module.core() {
    single {
        val storage = JsonStorage(
            typeToken = object : TypeToken<LocalConfigKeyValues>() {},
        )
        val file = get<JavaPlugin>()
            .dataFolder
            .resolve("config.json")
        if (!file.exists()) {
            storage.writeSync(file, LocalConfigKeyValues.default())
        }
        LocalConfig(
            file = file,
            storage = storage,
        )
    }

    single {
        OpenTelemetryProvider()
    }

    single {
        val localConfig = get<LocalConfig>()
        val config = localConfig.get()

        SentryProvider(
            dsn = config.observability.sentryDsn,
            environment = config.environment.name.lowercase(),
            traceSampleRate = config.observability.traceSampleRate,
        ).also {
            it.init()
        }
    } onClose {
        it?.close()
    }

    single {
        ErrorTracker(sentry = get())
    }

    factory {
        val config = get<RemoteConfig>().latest.config
        val zoneId = ZoneId.of(config.localization.timeZone)

        LocalizedTime(
            clock = Clock.system(zoneId),
        )
    }

    factory {
        val config = get<RemoteConfig>().latest.config

        DateTimeFormatter(
            locale =
                Locale.forLanguageTag(
                    config.localization.locale,
                ),
            timezone =
                ZoneId.of(
                    config.localization.timeZone,
                ),
        )
    }

    single {
        Store(
            file = get<JavaPlugin>()
                .dataFolder
                .resolve("cache/server_state.json"),
            storage = JsonStorage(
                typeToken = object : TypeToken<PersistedServerState>() {},
            ),
        )
    }

    single {
        RemoteConfig(
            configHttpService = get<PCBHttp>().config,
            eventBroadcaster = get(),
            file = get<JavaPlugin>()
                .dataFolder
                .resolve("cache/remote_config.json"),
            storage = JsonStorage(
                typeToken = object : TypeToken<RemoteConfigVersion>() {},
            ),
            errorTracker = get(),
        )
    }

    single {
        DiscordSend(
            localConfig = get(),
            discordHttpService = get<DiscordHttp>().discord,
            errorTracker = get(),
            periodicRunner = PeriodicRunner(processInterval = 10.seconds)
        )
    }

    factory {
        ManageUrlGenerator(localConfig = get())
    }

    factory {
        PlayerLookup(
            server = get(),
            playerDbMinecraftService = get<PlayerDbHttp>().minecraft,
        )
    }

    factory {
        PlayerTeleporter(
            safeYLocationFinder = get(),
            teleportHistoryStorage = get(),
        )
    }

    factory {
        TeleportHistoryStorage(
            eventBroadcaster = get(),
        )
    }

    factory { SafeYLocationFinder() }

    single { Cooldown(timer = get()) }
}

private fun Module.webServer() {
    single {
        val localConfig = get<LocalConfig>().get()

        HttpServer(
            config = HttpServerConfig(
                authToken = localConfig.webServer.token,
                port = localConfig.webServer.port,
            ),
            webhookDelegate = WebServerDelegate(
                eventBroadcaster = get(),
            ),
        )
    }
}

private fun Module.http() {
    single {
        val localConfig = get<LocalConfig>().get()

        PCBHttp(
            authToken = localConfig.api.token,
            baseURL = localConfig.api.baseUrl,
            httpLogger = if (localConfig.api.isLoggingEnabled) get() else null,
        )
    }

    single {
        val localConfig = get<LocalConfig>().get()

        DiscordHttp(
            httpLogger = if (localConfig.api.isLoggingEnabled) get() else null,
        )
    }

    single {
        val localConfig = get<LocalConfig>().get()

        PlayerDbHttp(
            httpLogger = if (localConfig.api.isLoggingEnabled) get() else null,
            userAgent = if (localConfig.environment.isProduction) "pcbmc.co"
                else ""
        )
    }

    factory { HttpLogger(logSync::trace) }
}

private fun Module.integrations() {
    single {
        DynmapIntegration(
            plugin = get(),
            remoteConfig = get(),
            spawnRepository = get(),
            warpRepository = get(),
        )
    }

    single {
        EssentialsIntegration(
            plugin = get(),
            server = get(),
            errorTracker = get(),
            store = get(),
            eventBroadcaster = get(),
            tabRenderer = get(),
        )
    }

    single {
        LuckPermsIntegration(
            permissions = get(),
        )
    }
}

private fun Module.architecture() {
    factory {
        PlayerStateListener(
            store = get(),
            time = get(),
            eventBroadcaster = get(),
            errorTracker = get(),
        )
    }

    factory {
        CoroutineExceptionListener(
            errorTracker = get(),
        )
    }

    single { ConnectionMiddlewareChain() }

    factory {
        AuthorizeConnectionListener(
            middlewareChain = get(),
            playerDataProvider = get(),
            errorTracker = get(),
            eventBroadcaster = get(),
        )
    }

    single { ChatDecoratorChain() }

    factory { AsyncChatListener(decorators = get()) }

    single { ServerListingDecoratorChain() }

    factory {
        ServerListPingListener(
            remoteConfig = get(),
            decorators = get(),
        )
    }

    single { Permissions() }

    single {
        TabRenderer(
            remoteConfig = get(),
            tabPlaceholders = get(),
        )
    }

    single {
        TabPlaceholders(
            listenerRegistry = get(),
        )
    }

    factory {
        TabListeners(
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        MaxPlayerCountPlaceholder(
            server = get(),
        )
    }

    factory {
        OnlinePlayerCountPlaceholder(
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        PlayerAFKPlaceholder(
            server = get(),
            tabRenderer = get(),
            store = get(),
        )
    }

    factory {
        PlayerNamePlaceholder()
    }

    factory {
        PlayerPingPlaceholder(
            plugin = get<JavaPlugin>(),
            server = get(),
            tabRenderer = get(),
            spigotTimer = get(),
        )
    }

    factory {
        PlayerWorldPlaceholder(
            tabRenderer = get(),
        )
    }
}
