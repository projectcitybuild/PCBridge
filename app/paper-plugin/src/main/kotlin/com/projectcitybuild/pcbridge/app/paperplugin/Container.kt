package com.projectcitybuild.pcbridge.app.paperplugin

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.datetime.services.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.discord.DiscordSend
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.core.http.logging.StructuredLoggingInterceptor
import com.projectcitybuild.pcbridge.core.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.core.localconfig.LocalConfigBacking
import com.projectcitybuild.pcbridge.core.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.core.localconfig.default
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.DiscordHttp
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.PCBHttp
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb.PlayerDbHttp
import com.projectcitybuild.pcbridge.core.utils.PeriodicRunner
import com.projectcitybuild.pcbridge.features.announcements.announcementsModule
import com.projectcitybuild.pcbridge.features.bans.bansModule
import com.projectcitybuild.pcbridge.features.building.buildingModule
import com.projectcitybuild.pcbridge.features.builds.buildsModule
import com.projectcitybuild.pcbridge.features.chatbadge.chatBadgeModule
import com.projectcitybuild.pcbridge.features.chatformatting.chatFormattingModule
import com.projectcitybuild.pcbridge.features.config.configModule
import com.projectcitybuild.pcbridge.features.homes.homesModule
import com.projectcitybuild.pcbridge.features.maintenance.maintenanceModule
import com.projectcitybuild.pcbridge.features.moderate.moderateModule
import com.projectcitybuild.pcbridge.features.onboarding.onboardingModule
import com.projectcitybuild.pcbridge.features.pim.pimModule
import com.projectcitybuild.pcbridge.features.register.registerModule
import com.projectcitybuild.pcbridge.features.roles.rolesModule
import com.projectcitybuild.pcbridge.features.serverlinks.serverLinksModule
import com.projectcitybuild.pcbridge.features.spawns.spawnsModule
import com.projectcitybuild.pcbridge.features.staffchat.staffChatModule
import com.projectcitybuild.pcbridge.features.stats.statsModule
import com.projectcitybuild.pcbridge.features.sync.syncModule
import com.projectcitybuild.pcbridge.features.teleportation.teleportationModule
import com.projectcitybuild.pcbridge.features.warnings.warningsModule
import com.projectcitybuild.pcbridge.features.warps.warpsModule
import com.projectcitybuild.pcbridge.features.watchdog.watchDogModule
import com.projectcitybuild.pcbridge.features.workstations.workstationsModule
import com.projectcitybuild.pcbridge.integrations.dynmap.dynmapModule
import com.projectcitybuild.pcbridge.integrations.essentials.essentialsModule
import com.projectcitybuild.pcbridge.integrations.luckperms.luckPermsModule
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.listeners.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.platform.paper.namespace.SpigotNamespace
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.platform.webserver.HttpServer
import com.projectcitybuild.pcbridge.platform.webserver.data.HttpServerConfig
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.runtime.playerlookup.PlayerLookup
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.runtimeModule
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import com.projectcitybuild.pcbridge.runtime.webhooks.WebServerDelegate
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module
import java.io.File
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

fun pluginModules(
    plugin: JavaPlugin,
    openTelemetryProvider: OpenTelemetryProvider,
) = buildList {
    add(mainModule(plugin, openTelemetryProvider))
    add(runtimeModule)
    addAll(featureModules)
    addAll(integrationModules)
}

fun createPCBridgeApplication(koin: Koin) = ApplicationCompositionRoot(koin).create()

private class ApplicationCompositionRoot(
    private val koin: Koin,
) : KoinComponent {
    override fun getKoin() = koin

    fun create() = PCBridgeApplication(
        errorTracker = get(),
        sentryProvider = get(),
        remoteConfig = get(),
        store = get(),
        featureRegistrar = get(),
        featureRegistrations = { koin.getAll<PaperFeatureRegistration>() },
        integrationRegistrar = get(),
        integrations = { koin.getAll<PaperIntegration>() },
        statsCollector = get(),
        discordSend = get(),
        httpServer = get(),
        timer = get(),
    )
}

private fun mainModule(
    plugin: JavaPlugin,
    openTelemetryProvider: OpenTelemetryProvider,
) = module {
    platform(plugin)
    core(openTelemetryProvider)
    http()
    webServer()
}

private val featureModules =
    listOf(
        announcementsModule,
        bansModule,
        buildingModule,
        buildsModule,
        chatBadgeModule,
        chatFormattingModule,
        configModule,
        homesModule,
        maintenanceModule,
        moderateModule,
        onboardingModule,
        pimModule,
        registerModule,
        rolesModule,
        serverLinksModule,
        spawnsModule,
        staffChatModule,
        statsModule,
        syncModule,
        teleportationModule,
        warningsModule,
        warpsModule,
        watchDogModule,
        workstationsModule,
    )

private val integrationModules =
    listOf(
        dynmapModule,
        essentialsModule,
        luckPermsModule,
    )

private fun Module.platform(plugin: JavaPlugin) {
    single { plugin }
    single { plugin as org.bukkit.plugin.Plugin }
    single<CommandTaskExecutor> { PluginCommandTaskExecutor(plugin) }

    factory { get<JavaPlugin>().server }

    singleOf(::SpigotNamespace)
    singleOf(::SpigotListenerRegistry)

    single {
        SpigotTimer(
            plugin = get(),
            errorTracker = get(),
        )
    }

    factory {
        SpigotEventBroadcaster(
            server = get(),
            minecraftDispatcher = { get<JavaPlugin>().minecraftDispatcher },
        )
    }
}

private fun Module.core(openTelemetryProvider: OpenTelemetryProvider) {
    single {
        val fileStorage =
            JsonFileStorage(
                typeToken = object : TypeToken<LocalConfigKeyValues>() {},
            )
        val file =
            get<JavaPlugin>()
                .dataFolder
                .resolve("config.json")
        if (!file.exists()) {
            fileStorage.writeSync(file, LocalConfigKeyValues.default())
        }
        LocalConfig(
            file = file,
            fileStorage =
                object : LocalConfigBacking<LocalConfigKeyValues> {
                    override fun readSync(file: File): LocalConfigKeyValues? = fileStorage.readSync(file)
                },
        )
    }

    single { openTelemetryProvider }

    single {
        val localConfig = get<LocalConfig>()
        val config = localConfig.get()

        SentryProvider(
            dsn = config.observability.sentryDsn,
            environment = config.environment.name.lowercase(),
            traceSampleRate = config.observability.traceSampleRate,
        )
    }

    singleOf(::ErrorTracker)

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
            file =
                get<JavaPlugin>()
                    .dataFolder
                    .resolve("cache/server_state.json"),
            fileStorage =
                JsonFileStorage(
                    typeToken = object : TypeToken<Map<String, JsonElement>>() {},
                ),
        )
    }

    singleOf(::SessionStore)

    single {
        RemoteConfig(
            configHttpService = get<PCBHttp>().config,
            eventBroadcaster = get(),
            file =
                get<JavaPlugin>()
                    .dataFolder
                    .resolve("cache/remote_config.json"),
            fileStorage =
                JsonFileStorage(
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
            periodicRunner = PeriodicRunner(processInterval = 10.seconds),
        )
    }

    factory {
        PlayerLookup(
            server = get(),
            playerDbMinecraftService = get<PlayerDbHttp>().minecraft,
        )
    }

    factoryOf(::PlayerTeleporter)
    factoryOf(::TeleportHistoryStorage)
    factoryOf(::SafeYLocationFinder)
    singleOf(::Cooldown)
}

private fun Module.webServer() {
    single {
        val localConfig = get<LocalConfig>().get()

        HttpServer(
            config =
                HttpServerConfig(
                    authToken = localConfig.webServer.token,
                    host = localConfig.webServer.host,
                    port = localConfig.webServer.port,
                ),
            webhookDelegate =
                WebServerDelegate(
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
            logger = if (localConfig.api.logLevel.enabled) get() else null,
            openTelemetry = get<OpenTelemetryProvider>().sdk,
        )
    }

    single {
        val localConfig = get<LocalConfig>().get()

        DiscordHttp(
            logger = if (localConfig.api.logLevel.enabled) get() else null,
            openTelemetry = get<OpenTelemetryProvider>().sdk,
        )
    }

    single {
        val localConfig = get<LocalConfig>().get()

        PlayerDbHttp(
            logger = if (localConfig.api.logLevel.enabled) get() else null,
            openTelemetry = get<OpenTelemetryProvider>().sdk,
            userAgent =
                if (localConfig.environment.isProduction) {
                    "pcbmc.co"
                } else {
                    ""
                },
        )
    }

    factory {
        val localConfig = get<LocalConfig>()

        StructuredLoggingInterceptor { message ->
            when (localConfig.get().api.logLevel) {
                LocalConfigKeyValues.Api.LogLevel.None -> return@StructuredLoggingInterceptor
                LocalConfigKeyValues.Api.LogLevel.Trace -> logSync.trace("http", message)
                LocalConfigKeyValues.Api.LogLevel.Debug -> logSync.debug("http", message)
                LocalConfigKeyValues.Api.LogLevel.Info -> logSync.info("http", message)
            }
        }
    }
}
