package com.projectcitybuild.plugin

import com.projectcitybuild.core.database.DataSource
import com.projectcitybuild.core.http.server.HTTPServer
import com.projectcitybuild.core.storage.adapters.YamlStorage
import com.projectcitybuild.features.aggregate.AuthoriseConnection
import com.projectcitybuild.features.aggregate.GetAggregate
import com.projectcitybuild.features.aggregate.SyncPlayerWithAggregate
import com.projectcitybuild.features.bans.usecases.BanIP
import com.projectcitybuild.features.bans.usecases.BanUUID
import com.projectcitybuild.features.bans.usecases.CheckUUIDBan
import com.projectcitybuild.features.bans.usecases.UnbanIP
import com.projectcitybuild.features.bans.usecases.UnbanUUID
import com.projectcitybuild.features.chat.ChatBadgeFormatter
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.features.chat.usecases.MutePlayer
import com.projectcitybuild.features.chat.usecases.ToggleBadge
import com.projectcitybuild.features.ranksync.usecases.GenerateAccountVerificationURL
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroups
import com.projectcitybuild.features.utilities.usecases.GetVersion
import com.projectcitybuild.features.utilities.usecases.ReloadPlugin
import com.projectcitybuild.features.warnings.usecases.AcknowledgeWarning
import com.projectcitybuild.features.warnings.usecases.GetUnacknowledgedWarnings
import com.projectcitybuild.features.warps.usecases.CreateWarp
import com.projectcitybuild.features.warps.usecases.DeleteWarp
import com.projectcitybuild.features.warps.usecases.GetWarpList
import com.projectcitybuild.features.warps.usecases.TeleportToWarp
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.adapters.YamlKeyValueStorage
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import com.projectcitybuild.modules.datetime.time.LocalizedTime
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.errorreporting.adapters.SentryErrorReporter
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.modules.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import com.projectcitybuild.pcbridge.http.clients.MojangClient
import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import com.projectcitybuild.pcbridge.http.core.APIClientImpl
import com.projectcitybuild.plugin.commands.ACommand
import com.projectcitybuild.plugin.commands.BadgeCommand
import com.projectcitybuild.plugin.commands.BanCommand
import com.projectcitybuild.plugin.commands.BanIPCommand
import com.projectcitybuild.plugin.commands.CheckBanCommand
import com.projectcitybuild.plugin.commands.DelWarpCommand
import com.projectcitybuild.plugin.commands.MuteCommand
import com.projectcitybuild.plugin.commands.PCBridgeCommand
import com.projectcitybuild.plugin.commands.SetWarpCommand
import com.projectcitybuild.plugin.commands.SyncCommand
import com.projectcitybuild.plugin.commands.SyncOtherCommand
import com.projectcitybuild.plugin.commands.UnbanCommand
import com.projectcitybuild.plugin.commands.UnbanIPCommand
import com.projectcitybuild.plugin.commands.UnmuteCommand
import com.projectcitybuild.plugin.commands.WarningAcknowledgeCommand
import com.projectcitybuild.plugin.commands.WarpCommand
import com.projectcitybuild.plugin.commands.WarpsCommand
import com.projectcitybuild.plugin.integrations.dynmap.DynmapMarkerIntegration
import com.projectcitybuild.plugin.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.plugin.integrations.gadgetsmenu.GadgetsMenuIntegration
import com.projectcitybuild.plugin.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.plugin.listeners.AsyncPlayerChatListener
import com.projectcitybuild.plugin.listeners.AsyncPreLoginListener
import com.projectcitybuild.plugin.listeners.ExceptionListener
import com.projectcitybuild.plugin.listeners.FirstTimeJoinListener
import com.projectcitybuild.plugin.listeners.PlayerJoinListener
import com.projectcitybuild.plugin.listeners.PlayerQuitListener
import com.projectcitybuild.plugin.listeners.TelemetryListener
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
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.commands.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.support.spigot.eventbroadcast.SpigotLocalEventBroadcaster
import com.projectcitybuild.support.spigot.kick.PlayerKicker
import com.projectcitybuild.support.spigot.kick.SpigotPlayerKicker
import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
import com.projectcitybuild.support.spigot.logger.Logger
import com.projectcitybuild.support.spigot.logger.SpigotLogger
import com.projectcitybuild.support.spigot.scheduler.Scheduler
import com.projectcitybuild.support.spigot.scheduler.SpigotScheduler
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
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
        get() = LocalizedTime()

    val logger: Logger
        get() = SpigotLogger(spigotLogger)

    val scheduler: Scheduler
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

    val pcbClient by lazy {
        PCBClient(
            authToken = config.get(ConfigKeys.apiToken),
            baseUrl = config.get(ConfigKeys.apiBaseURL),
            withLogging = config.get(ConfigKeys.apiIsLoggingEnabled)
        )
    }

    val mojangClient by lazy {
        MojangClient(
            withLogging = config.get(ConfigKeys.apiIsLoggingEnabled)
        )
    }

    val apiClient: APIClient by lazy {
        APIClientImpl { minecraftDispatcher }
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

    val playerKicker: PlayerKicker
        get() = SpigotPlayerKicker(server)

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

    val httpServer: HTTPServer by lazy {
        HTTPServer(
            scheduler,
            server,
            config,
            logger,
            UpdatePlayerGroups(
                permissions,
                playerGroupRepository,
            ),
        )
    }

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
        PlayerBanRepository(
            pcbClient,
            apiClient,
        )
    }

    val playerUUIDRepository by lazy {
        PlayerUUIDRepository(
            server,
            mojangClient,
            apiClient,
        )
    }

    val playerGroupRepository by lazy {
        PlayerGroupRepository(
            pcbClient,
            apiClient,
            config,
            logger,
        )
    }

    val playerWarningRepository by lazy {
        PlayerWarningRepository(
            pcbClient,
            apiClient,
        )
    }

    val ipBanRepository by lazy {
        IPBanRepository(
            pcbClient,
            apiClient,
        )
    }

    val warpRepository by lazy {
        WarpRepository(dataSource)
    }

    val aggregateRepository by lazy {
        AggregateRepository(
            pcbClient,
            apiClient,
        )
    }

    val telemetryRepository by lazy {
        TelemetryRepository(
            pcbClient,
            apiClient,
        )
    }

    val currencyRepository by lazy {
        CurrencyRepository(
            pcbClient,
            apiClient,
        )
    }

    /**
     * Commands
     */

    val aCommand get() = ACommand(server)

    val badgeCommand get() = BadgeCommand(
        ToggleBadge(playerConfigRepository)
    )

    val banCommand get() = BanCommand(
        server,
        BanUUID(
            playerBanRepository,
            playerUUIDRepository,
            server,
            playerKicker,
        ),
    )

    val banIPCommand get() = BanIPCommand(
        server,
        BanIP(ipBanRepository, playerKicker),
    )

    val checkBanCommand get() = CheckBanCommand(
        server,
        CheckUUIDBan(
            playerBanRepository,
            playerUUIDRepository,
            dateTimeFormatter,
        ),
    )

    val delWarpCommand get() = DelWarpCommand(
        DeleteWarp(
            warpRepository,
            localEventBroadcaster,
        ),
        warpRepository,
    )

    val muteCommand get() = MuteCommand(
        server,
        MutePlayer(
            playerConfigRepository,
            nameGuesser,
        )
    )

    val pcbridgeCommand get() = PCBridgeCommand(
        GetVersion(),
        ReloadPlugin(
            chatGroupFormatter,
            playerConfigCache,
            warpRepository,
            config,
        )
    )

    val setWarpCommand get() = SetWarpCommand(
        CreateWarp(
            warpRepository,
            localEventBroadcaster,
            time,
        )
    )

    val syncCommand get() = SyncCommand(
        GenerateAccountVerificationURL(
            pcbClient,
            apiClient
        ),
        UpdatePlayerGroups(
            permissions,
            playerGroupRepository,
        )
    )

    val syncOtherCommand get() = SyncOtherCommand(
        server,
        UpdatePlayerGroups(
            permissions,
            playerGroupRepository,
        ),
        nameGuesser,
    )

    val unbanCommand get() = UnbanCommand(
        server,
        UnbanUUID(
            playerBanRepository,
            playerUUIDRepository,
            server,
        )
    )

    val unbanIPCommand get() = UnbanIPCommand(
        UnbanIP(ipBanRepository)
    )

    val unmuteCommand get() = UnmuteCommand(
        server,
        MutePlayer(
            playerConfigRepository,
            nameGuesser,
        ),
    )

    val warningAcknowledgeCommand get() = WarningAcknowledgeCommand(
        AcknowledgeWarning(playerWarningRepository)
    )

    val warpCommand get() = WarpCommand(
        TeleportToWarp(
            warpRepository,
            nameGuesser,
            logger,
            localEventBroadcaster,
            server,
        ),
        warpRepository,
    )

    val warpsCommand get() = WarpsCommand(
        GetWarpList(
            warpRepository,
            config,
        )
    )

    /**
     * Listeners
     */

    val asyncPlayerChatListener get() = AsyncPlayerChatListener(
        server,
        playerConfigRepository,
        chatGroupFormatter,
        chatBadgeFormatter,
    )

    val asyncPreLoginListener get() = AsyncPreLoginListener(
        GetAggregate(aggregateRepository),
        AuthoriseConnection(),
        SyncPlayerWithAggregate(
            permissions,
            chatBadgeRepository,
            config,
            logger,
        ),
        logger,
        dateTimeFormatter,
        errorReporter,
    )

    val exceptionListener get() = ExceptionListener(
        errorReporter,
    )

    val firstTimeJoinListener get() = FirstTimeJoinListener(
        server,
        logger,
    )

    val playerJoinListener get() = PlayerJoinListener(
        server,
        localEventBroadcaster,
        playerConfigRepository,
        GetUnacknowledgedWarnings(
            playerWarningRepository,
            dateTimeFormatter,
        ),
        logger,
    )

    val playerQuitListener get() = PlayerQuitListener(
        server,
        playerConfigCache,
        chatGroupFormatter,
        chatBadgeRepository,
    )

    val telemetryListener get() = TelemetryListener(
        telemetryRepository,
    )

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
