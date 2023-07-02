package com.projectcitybuild

import com.projectcitybuild.commands.ACommand
import com.projectcitybuild.commands.BadgeCommand
import com.projectcitybuild.commands.BanCommand
import com.projectcitybuild.commands.BanIPCommand
import com.projectcitybuild.commands.CheckBanCommand
import com.projectcitybuild.commands.DelWarpCommand
import com.projectcitybuild.commands.MuteCommand
import com.projectcitybuild.commands.PCBridgeCommand
import com.projectcitybuild.commands.SetWarpCommand
import com.projectcitybuild.commands.SyncCommand
import com.projectcitybuild.commands.SyncOtherCommand
import com.projectcitybuild.commands.UnbanCommand
import com.projectcitybuild.commands.UnbanIPCommand
import com.projectcitybuild.commands.UnmuteCommand
import com.projectcitybuild.commands.WarningAcknowledgeCommand
import com.projectcitybuild.commands.WarpCommand
import com.projectcitybuild.commands.WarpsCommand
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
import com.projectcitybuild.integrations.dynmap.DynmapMarkerIntegration
import com.projectcitybuild.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.integrations.gadgetsmenu.GadgetsMenuIntegration
import com.projectcitybuild.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.listeners.AsyncPlayerChatListener
import com.projectcitybuild.listeners.AsyncPreLoginListener
import com.projectcitybuild.listeners.ExceptionListener
import com.projectcitybuild.listeners.FirstTimeJoinListener
import com.projectcitybuild.listeners.PlayerJoinListener
import com.projectcitybuild.listeners.PlayerQuitListener
import com.projectcitybuild.listeners.TelemetryListener
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.adapters.YamlKeyValueStorage
import com.projectcitybuild.modules.database.DataSource
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
import com.projectcitybuild.modules.storage.adapters.YamlStorage
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
import com.projectcitybuild.pcbridge.http.clients.MojangClient
import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.mojang.PlayerUUIDHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import com.projectcitybuild.pcbridge.http.services.pcb.AggregateHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.CurrencyHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerWarningHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.TelemetryHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
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
import com.projectcitybuild.support.spigot.SpigotScheduler
import com.projectcitybuild.support.spigot.commands.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.support.spigot.eventbroadcast.SpigotLocalEventBroadcaster
import com.projectcitybuild.support.spigot.kick.PlayerKicker
import com.projectcitybuild.support.spigot.kick.SpigotPlayerKicker
import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
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

    private val localEventBroadcaster: LocalEventBroadcaster
        get() = SpigotLocalEventBroadcaster()

    private val playerKicker: PlayerKicker
        get() = SpigotPlayerKicker(server)

    private val nameGuesser
        get() = NameGuesser()

    val permissions: Permissions by lazy {
        LuckPermsPermissions(logger)
    }

    private val chatGroupFormatter by lazy {
        ChatGroupFormatter(
            permissions,
            config,
        )
    }

    private val chatBadgeFormatter by lazy {
        ChatBadgeFormatter(
            playerConfigRepository,
            chatBadgeRepository,
            config,
        )
    }

    val httpServer: HttpServer by lazy {
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

    /**
     * HTTP
     */

    private val pcbClient by lazy {
        PCBClientFactory(
            authToken = config.get(ConfigKeys.apiToken),
            baseUrl = config.get(ConfigKeys.apiBaseURL),
            withLogging = config.get(ConfigKeys.apiIsLoggingEnabled)
        ).build()
    }

    private val mojangClient by lazy {
        MojangClient(
            withLogging = config.get(ConfigKeys.apiIsLoggingEnabled)
        ).build()
    }

    private val responseParser: ResponseParser by lazy {
        ResponseParser { minecraftDispatcher }
    }

    /**
     * Repositories
     */

    private val chatBadgeRepository by lazy {
        ChatBadgeRepository()
    }

    private val playerConfigCache by lazy {
        PlayerConfigCache()
    }

    private val playerConfigRepository by lazy {
        PlayerConfigRepository(
            cache = playerConfigCache,
            dataSource,
        )
    }

    private val playerBanRepository by lazy {
        PlayerBanRepository(
            UUIDBanHttpService(pcbClient, responseParser),
        )
    }

    private val playerUUIDRepository by lazy {
        PlayerUUIDRepository(
            server,
            PlayerUUIDHttpService(mojangClient, responseParser),
        )
    }

    private val playerGroupRepository by lazy {
        PlayerGroupRepository(
            PlayerGroupHttpService(pcbClient, responseParser),
            config,
            logger,
        )
    }

    private val playerWarningRepository by lazy {
        PlayerWarningRepository(
            PlayerWarningHttpService(pcbClient, responseParser),
        )
    }

    private val ipBanRepository by lazy {
        IPBanRepository(
            IPBanHttpService(pcbClient, responseParser),
        )
    }

    private val warpRepository by lazy {
        WarpRepository(dataSource)
    }

    private val aggregateRepository by lazy {
        AggregateRepository(
            AggregateHttpService(pcbClient, responseParser),
        )
    }

    private val telemetryRepository by lazy {
        TelemetryRepository(
            TelemetryHttpService(pcbClient, responseParser),
        )
    }

    private val currencyRepository by lazy {
        CurrencyRepository(
            CurrencyHttpService(pcbClient, responseParser),
        )
    }

    private val verificationURLRepository by lazy {
        VerificationURLRepository(
            AccountLinkHTTPService(pcbClient, responseParser),
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
            verificationURLRepository,
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
