package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.http.discord.DiscordHttp
import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.playerdb.PlayerDbHttp
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.DateTimeFormatter
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.discord.DiscordSend
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.default
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.utils.PeriodicRunner
import com.projectcitybuild.pcbridge.paper.features.announcements.announcementsModule
import com.projectcitybuild.pcbridge.paper.features.bans.bansModule
import com.projectcitybuild.pcbridge.paper.features.building.buildingModule
import com.projectcitybuild.pcbridge.paper.features.builds.buildsModule
import com.projectcitybuild.pcbridge.paper.features.chatbadge.chatBadgeModule
import com.projectcitybuild.pcbridge.paper.features.chatformatting.chatFormattingModule
import com.projectcitybuild.pcbridge.paper.features.config.configModule
import com.projectcitybuild.pcbridge.paper.features.homes.homesModule
import com.projectcitybuild.pcbridge.paper.features.maintenance.maintenanceModule
import com.projectcitybuild.pcbridge.paper.features.moderate.moderateModule
import com.projectcitybuild.pcbridge.paper.features.onboarding.onboardingModule
import com.projectcitybuild.pcbridge.paper.features.pim.pimModule
import com.projectcitybuild.pcbridge.paper.features.randomteleport.randomTeleportModule
import com.projectcitybuild.pcbridge.paper.features.register.registerModule
import com.projectcitybuild.pcbridge.paper.features.roles.rolesModule
import com.projectcitybuild.pcbridge.paper.features.serverlinks.serverLinksModule
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsModule
import com.projectcitybuild.pcbridge.paper.features.staffchat.staffChatModule
import com.projectcitybuild.pcbridge.paper.features.stats.statsModule
import com.projectcitybuild.pcbridge.paper.features.sync.syncModule
import com.projectcitybuild.pcbridge.paper.features.warnings.warningsModule
import com.projectcitybuild.pcbridge.paper.features.warps.warpsModule
import com.projectcitybuild.pcbridge.paper.features.watchdog.watchDogModule
import com.projectcitybuild.pcbridge.paper.features.workstations.workstationsModule
import com.projectcitybuild.pcbridge.paper.integrations.dynmap.dynmapModule
import com.projectcitybuild.pcbridge.paper.integrations.essentials.essentialsModule
import com.projectcitybuild.pcbridge.paper.integrations.luckperms.luckPermsModule
import com.projectcitybuild.pcbridge.paper.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.platform.paper.listeners.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.platform.paper.namespace.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.paper.runtime.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.paper.runtime.playerlookup.PlayerLookup
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.runtime.runtimeModule
import com.projectcitybuild.pcbridge.paper.runtime.state.data.PersistedServerState
import com.projectcitybuild.pcbridge.paper.runtime.state.store.SessionStore
import com.projectcitybuild.pcbridge.paper.runtime.state.store.Store
import com.projectcitybuild.pcbridge.paper.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.runtime.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.paper.runtime.teleportation.storage.TeleportHistoryStorage
import com.projectcitybuild.pcbridge.paper.runtime.webhooks.WebServerDelegate
import com.projectcitybuild.pcbridge.webserver.HttpServer
import com.projectcitybuild.pcbridge.webserver.data.HttpServerConfig
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

fun pluginModules(plugin: JavaPlugin) =
    buildList {
        add(mainModule(plugin))
        add(runtimeModule)
        addAll(featureModules)
        addAll(integrationModules)
    }

private fun mainModule(plugin: JavaPlugin) =
    module {
        platform(plugin)
        core()
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
        randomTeleportModule,
        registerModule,
        rolesModule,
        serverLinksModule,
        spawnsModule,
        staffChatModule,
        statsModule,
        syncModule,
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

    factory { get<JavaPlugin>().server }

    singleOf(::SpigotNamespace)
    singleOf(::SpigotListenerRegistry)
    factoryOf(::SpigotTimer)

    factory {
        SpigotEventBroadcaster(
            server = get(),
            minecraftDispatcher = { get<JavaPlugin>().minecraftDispatcher },
        )
    }
}

private fun Module.core() {
    single {
        val storage =
            JsonStorage(
                typeToken = object : TypeToken<LocalConfigKeyValues>() {},
            )
        val file =
            get<JavaPlugin>()
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
            storage =
                JsonStorage(
                    typeToken = object : TypeToken<PersistedServerState>() {},
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
            storage =
                JsonStorage(
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
