// package com.projectcitybuild.deprecated
//
// import com.google.gson.reflect.TypeToken
// import com.projectcitybuild.core.config.PluginConfig
// import com.projectcitybuild.modules.chat.ChatBadgeFormatter
// import com.projectcitybuild.modules.chat.ChatGroupFormatter
// import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
// import com.projectcitybuild.integrations.dynmap.DynmapMarkerIntegration
// import com.projectcitybuild.integrations.essentials.EssentialsIntegration
// import com.projectcitybuild.integrations.luckperms.LuckPermsIntegration
// import com.projectcitybuild.pcbridge.core.modules.config.Config
// import com.projectcitybuild.core.database.SQLDataSource
// import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatter
// import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatterImpl
// import com.projectcitybuild.pcbridge.core.modules.datetime.time.LocalizedTime
// import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
// import com.projectcitybuild.libs.errorreporting.ErrorReporter
// import com.projectcitybuild.libs.errorreporting.outputs.PrintStackTraceOutput
// import com.projectcitybuild.core.errors.SentryErrorOutput
// import com.projectcitybuild.libs.nameguesser.NameGuesser
// import com.projectcitybuild.libs.permissions.Permissions
// import com.projectcitybuild.libs.permissions.adapters.LuckPermsPermissions
// import com.projectcitybuild.libs.playercache.PlayerConfigCache
// import com.projectcitybuild.pcbridge.core.storage.JsonStorage
// import com.projectcitybuild.modules.joinmessages.PlayerJoinTimeCache
// import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
// import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
// import com.projectcitybuild.pcbridge.core.contracts.PlatformTimer
// import com.projectcitybuild.pcbridge.core.modules.filecache.FileCache
// import com.projectcitybuild.pcbridge.http.HttpService
// import com.projectcitybuild.pcbridge.webserver.HttpServer
// import com.projectcitybuild.pcbridge.webserver.HttpServerConfig
// import com.projectcitybuild.repositories.*
// import com.projectcitybuild.support.spigot.SpigotLogger
// import com.projectcitybuild.support.spigot.SpigotNamespace
// import com.projectcitybuild.support.spigot.SpigotScheduler
// import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
// import com.projectcitybuild.support.spigot.eventbroadcast.SpigotLocalEventBroadcaster
// import com.projectcitybuild.support.spigot.listeners.SpigotListenerRegistry
// import com.projectcitybuild.support.spigot.SpigotServer
// import com.projectcitybuild.support.spigot.SpigotTimer
// import org.bukkit.Server
// import org.bukkit.plugin.java.JavaPlugin
// import java.time.Clock
// import java.time.ZoneId
// import java.util.Locale
// import kotlin.coroutines.CoroutineContext
// import java.util.logging.Logger as JavaLogger
//
// class DependencyContainer(
//     val plugin: JavaPlugin,
//     val server: Server,
//     val spigotLogger: JavaLogger,
//     val minecraftDispatcher: CoroutineContext,
// ) {
//     val config: Config<PluginConfig> by lazy {
//         Config(
//             default = PluginConfig.default,
//             jsonStorage = JsonStorage(
//                 file = plugin.dataFolder.resolve("config.json"),
//                 logger = logger,
//                 typeToken = object : TypeToken<PluginConfig>(){},
//             ),
//         )
//     }
//
//     // TODO: inject config instead of the config keys, otherwise flushing the cache
//     // will never do anything
//     val dateTimeFormatter: DateTimeFormatter
//         get() = DateTimeFormatterImpl(
//             locale = Locale.forLanguageTag(
//                 config.get().localization.locale,
//             ),
//             timezone = ZoneId.of(
//                 config.get().localization.timeZone
//             ),
//         )
//
//     val time: Time
//         get() = LocalizedTime(
//             clock = Clock.system(
//                 ZoneId.of(config.get().localization.timeZone),
//             ),
//         )
//
//     val logger: PlatformLogger
//         get() = SpigotLogger(spigotLogger)
//
//     val scheduler: PlatformScheduler
//         get() = SpigotScheduler(plugin)
//
//     val timer: PlatformTimer by lazy {
//         SpigotTimer(plugin)
//     }
//
//     val errorReporter: ErrorReporter by lazy {
//         ErrorReporter(
//             outputs = listOf(
//                 SentryErrorOutput(config, logger),
//                 PrintStackTraceOutput(),
//             )
//         )
//     }
//
//     val dataSource by lazy {
//         SQLDataSource(
//             logger = logger,
//             hostName = config.get().database.hostName,
//             port = config.get().database.port,
//             databaseName = config.get().database.name,
//             username = config.get().database.username,
//             password = config.get().database.password,
//             shouldRunMigrations = true,
//         )
//     }
//
//     val listenerRegistry by lazy {
//         SpigotListenerRegistry(
//             plugin,
//             logger,
//         )
//     }
//
//     val localEventBroadcaster: LocalEventBroadcaster
//         get() = SpigotLocalEventBroadcaster(scheduler)
//
//     val spigotServer: SpigotServer
//         get() = SpigotServer(server)
//
//     val nameGuesser
//         get() = NameGuesser()
//
//     val permissions: Permissions by lazy {
//         LuckPermsPermissions(logger)
//     }
//
//     val chatGroupFormatter by lazy {
//         ChatGroupFormatter(
//             permissions,
//             config,
//         )
//     }
//
//     val chatBadgeFormatter by lazy {
//         ChatBadgeFormatter(
//             playerConfigRepository,
//             chatBadgeRepository,
//             config,
//         )
//     }
//
//     private val httpService by lazy {
//         HttpService(
//             authToken = config.get().api.token,
//             baseURL = config.get().api.baseUrl,
//             withLogging = config.get().api.isLoggingEnabled,
//             contextBuilder = { minecraftDispatcher },
//         )
//     }
//
//     val webServer by lazy {
//         HttpServer(
//             config = HttpServerConfig(
//                 authToken = config.get().webServer.token,
//                 port = config.get().webServer.port,
//             ),
//             delegate = WebServerDelegate(
//                 scheduler,
//                 server,
//                 logger,
//                 UpdatePlayerGroups(
//                     permissions,
//                     playerGroupRepository,
//                 ),
//             ),
//             logger = logger,
//         )
//     }
//
//     val spigotNamespace get() = SpigotNamespace(plugin)
//
//     val playerJoinTimeCache by lazy {
//         PlayerJoinTimeCache(time)
//     }
//
//     /**
//      * Repositories
//      */
//
//     val chatBadgeRepository by lazy {
//         ChatBadgeRepository()
//     }
//
//     val playerConfigCache by lazy {
//         PlayerConfigCache()
//     }
//
//     val playerConfigRepository by lazy {
//         PlayerConfigRepository(
//             cache = playerConfigCache,
//             dataSource,
//         )
//     }
//
//     val playerBanRepository by lazy {
//         PlayerBanRepository(httpService.uuidBan)
//     }
//
//     val playerUUIDRepository by lazy {
//         PlayerUUIDRepository(
//             server,
//             httpService.playerUuid,
//         )
//     }
//
//     val playerGroupRepository by lazy {
//         PlayerGroupRepository(
//             httpService.playerGroup,
//             config,
//             logger,
//         )
//     }
//
//     val playerWarningRepository by lazy {
//         PlayerWarningRepository(
//             httpService.playerWarning,
//         )
//     }
//
//     val ipBanRepository by lazy {
//         IPBanRepository(
//             httpService.ipBan,
//         )
//     }
//
//     val warpRepository by lazy {
//         WarpRepository(dataSource)
//     }
//
//     val aggregateRepository by lazy {
//         AggregateRepository(
//             httpService.aggregate,
//         )
//     }
//
//     val telemetryRepository by lazy {
//         TelemetryRepository(
//             httpService.telemetry,
//         )
//     }
//
//     val verificationURLRepository by lazy {
//         VerificationURLRepository(
//             httpService.verificationURL,
//         )
//     }
//
//     val scheduledAnnouncementsRepository by lazy {
//         ScheduledAnnouncementsRepository(
//             config,
//             FileCache(
//                 JsonStorage(
//                     file = plugin.dataFolder.resolve("cache/scheduled_announcements.json"),
//                     logger = logger,
//                     typeToken = object : TypeToken<ScheduledAnnouncements>(){},
//                 ),
//             ),
//         )
//     }
//
//     /**
//      * Integrations
//      */
//
//     val dynmapIntegration by lazy {
//         DynmapMarkerIntegration(
//             plugin,
//             warpRepository,
//             config,
//             logger,
//         )
//     }
//
//     val essentialsIntegration by lazy {
//         EssentialsIntegration(
//             plugin,
//             logger,
//         )
//     }
//
//     val luckPermsIntegration by lazy {
//         LuckPermsIntegration(
//             plugin,
//             logger,
//             chatGroupFormatter,
//         )
//     }
// }
