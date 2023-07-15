package com.projectcitybuild

import com.projectcitybuild.modules.chat.ChatBadgeFormatter
import com.projectcitybuild.modules.chat.ChatGroupFormatter
import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.integrations.dynmap.DynmapMarkerIntegration
import com.projectcitybuild.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.integrations.gadgetsmenu.GadgetsMenuIntegration
import com.projectcitybuild.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.libs.config.Config
import com.projectcitybuild.libs.config.ConfigKeys
import com.projectcitybuild.libs.config.adapters.YamlKeyValueStorage
import com.projectcitybuild.libs.database.DataSource
import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatterImpl
import com.projectcitybuild.pcbridge.core.modules.datetime.time.LocalizedTime
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.libs.errorreporting.adapters.SentryErrorReporter
import com.projectcitybuild.libs.nameguesser.NameGuesser
import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.libs.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.libs.playercache.PlayerConfigCache
import com.projectcitybuild.libs.storage.adapters.YamlStorage
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
import com.projectcitybuild.pcbridge.http.HttpService
import com.projectcitybuild.pcbridge.webserver.HttpServer
import com.projectcitybuild.pcbridge.webserver.HttpServerConfig
import com.projectcitybuild.repositories.AggregateRepository
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.repositories.CurrencyRepository
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.repositories.PlayerGroupRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.repositories.PlayerWarningRepository
import com.projectcitybuild.repositories.TelemetryRepository
import com.projectcitybuild.repositories.VerificationURLRepository
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.SpigotLogger
import com.projectcitybuild.support.spigot.SpigotNamespace
import com.projectcitybuild.support.spigot.SpigotScheduler
import com.projectcitybuild.support.spigot.commands.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.support.spigot.eventbroadcast.SpigotLocalEventBroadcaster
import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
import com.projectcitybuild.support.spigot.SpigotServer
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import java.util.logging.Logger as JavaLogger

class DependencyContainer(
    val plugin: JavaPlugin,
    val server: Server,
    val spigotConfig: FileConfiguration,
    val spigotLogger: JavaLogger,
    val minecraftDispatcher: CoroutineContext,
) {
    val config: Config by lazy {
        Config(
            YamlKeyValueStorage(
                storage = YamlStorage(spigotConfig)
            )
        )
    }

    val dateTimeFormatter: DateTimeFormatter
        get() = DateTimeFormatterImpl(
            locale = Locale.forLanguageTag(
                config.get(ConfigKeys.timeLocale)
            ),
            timezone = ZoneId.of(
                config.get(ConfigKeys.timeTimezone)
            ),
        )

    val time: Time
        get() = LocalizedTime(
            clock = Clock.system(
                ZoneId.of(config.get(ConfigKeys.timeTimezone))
            ),
        )

    val logger: PlatformLogger
        get() = SpigotLogger(spigotLogger)

    val scheduler: PlatformScheduler
        get() = SpigotScheduler(plugin)

    val errorReporter: ErrorReporter by lazy {
        SentryErrorReporter(config, logger)
    }

    val dataSource by lazy {
        DataSource(
            logger = logger,
            hostName = config.get(ConfigKeys.dbHostName),
            port = config.get(ConfigKeys.dbPort),
            databaseName = config.get(ConfigKeys.dbName),
            username = config.get(ConfigKeys.dbUsername),
            password = config.get(ConfigKeys.dbPassword),
            shouldRunMigrations = true
        )
    }

    val listenerRegistry by lazy {
        SpigotListenerRegistry(
            plugin,
            logger,
        )
    }

    val commandRegistry by lazy {
        SpigotCommandRegistry(
            plugin,
            logger,
            errorReporter,
        )
    }

    val localEventBroadcaster: LocalEventBroadcaster
        get() = SpigotLocalEventBroadcaster()

    val spigotServer: SpigotServer
        get() = SpigotServer(server)

    val nameGuesser
        get() = NameGuesser()

    val permissions: Permissions by lazy {
        LuckPermsPermissions(logger)
    }

    val chatGroupFormatter by lazy {
        ChatGroupFormatter(
            permissions,
            config,
        )
    }

    val chatBadgeFormatter by lazy {
        ChatBadgeFormatter(
            playerConfigRepository,
            chatBadgeRepository,
            config,
        )
    }

    private val httpService by lazy {
        HttpService(
            authToken = config.get(ConfigKeys.apiToken),
            baseURL = config.get(ConfigKeys.apiBaseURL),
            withLogging = config.get(ConfigKeys.apiIsLoggingEnabled),
            contextBuilder = { minecraftDispatcher },
        )
    }

    val webServer by lazy {
        HttpServer(
            config = HttpServerConfig(
                authToken = config.get(ConfigKeys.internalWebServerToken),
                port = config.get(ConfigKeys.internalWebServerPort),
            ),
            delegate = WebServerDelegate(
                scheduler,
                server,
                logger,
                UpdatePlayerGroups(
                    permissions,
                    playerGroupRepository,
                ),
            ),
            logger = logger,
        )
    }

    val spigotNamespace get() = SpigotNamespace(plugin)

    /**
     * Repositories
     */

    val chatBadgeRepository by lazy {
        ChatBadgeRepository()
    }

    val playerConfigCache by lazy {
        PlayerConfigCache()
    }

    val playerConfigRepository by lazy {
        PlayerConfigRepository(
            cache = playerConfigCache,
            dataSource,
        )
    }

    val playerBanRepository by lazy {
        PlayerBanRepository(httpService.uuidBan)
    }

    val playerUUIDRepository by lazy {
        PlayerUUIDRepository(
            server,
            httpService.playerUuid,
        )
    }

    val playerGroupRepository by lazy {
        PlayerGroupRepository(
            httpService.playerGroup,
            config,
            logger,
        )
    }

    val playerWarningRepository by lazy {
        PlayerWarningRepository(
            httpService.playerWarning,
        )
    }

    val ipBanRepository by lazy {
        IPBanRepository(
            httpService.ipBan,
        )
    }

    val warpRepository by lazy {
        WarpRepository(dataSource)
    }

    val aggregateRepository by lazy {
        AggregateRepository(
            httpService.aggregate,
        )
    }

    val telemetryRepository by lazy {
        TelemetryRepository(
            httpService.telemetry,
        )
    }

    private val currencyRepository by lazy {
        CurrencyRepository(
            httpService.currency,
        )
    }

    val verificationURLRepository by lazy {
        VerificationURLRepository(
            httpService.verificationURL,
        )
    }

    /**
     * Integrations
     */

    val dynmapIntegration by lazy {
        DynmapMarkerIntegration(
            plugin,
            warpRepository,
            config,
            logger,
        )
    }

    val essentialsIntegration by lazy {
        EssentialsIntegration(
            plugin,
            logger,
        )
    }

    val gadgetsMenuIntegration by lazy {
        GadgetsMenuIntegration(
            plugin,
            logger,
            currencyRepository,
        )
    }

    val luckPermsIntegration by lazy {
        LuckPermsIntegration(
            plugin,
            logger,
            chatGroupFormatter,
        )
    }
}
