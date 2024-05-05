package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.core.database.DatabaseSource
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.core.permissions.Permissions
import com.projectcitybuild.core.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.core.state.Store
import com.projectcitybuild.features.bans.actions.AuthoriseConnection
import com.projectcitybuild.features.bans.actions.BanIP
import com.projectcitybuild.features.bans.actions.BanUUID
import com.projectcitybuild.features.bans.actions.CheckUUIDBan
import com.projectcitybuild.features.bans.actions.UnbanIP
import com.projectcitybuild.features.bans.actions.UnbanUUID
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.BanIPCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.features.bans.commands.UnbanIPCommand
import com.projectcitybuild.features.bans.listeners.AuthorizeConnectionListener
import com.projectcitybuild.features.bans.repositories.AggregateRepository
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.features.chat.ChatBadgeFormatter
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.features.chat.listeners.EmojiChatListener
import com.projectcitybuild.features.chat.listeners.FormatNameChatListener
import com.projectcitybuild.features.chat.listeners.SyncBadgesOnJoinListener
import com.projectcitybuild.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.features.chat.repositories.ChatGroupRepository
import com.projectcitybuild.features.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.features.invisframes.listeners.FrameItemInsertListener
import com.projectcitybuild.features.invisframes.listeners.FrameItemRemoveListener
import com.projectcitybuild.features.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.features.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.features.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.features.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.features.mute.commands.MuteCommand
import com.projectcitybuild.features.mute.commands.UnmuteCommand
import com.projectcitybuild.features.mute.listeners.MuteChatListener
import com.projectcitybuild.features.nightvision.commands.NightVisionCommand
import com.projectcitybuild.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.features.sync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.features.sync.actions.SyncPlayerGroups
import com.projectcitybuild.features.sync.actions.UpdatePlayerGroups
import com.projectcitybuild.features.sync.commands.SyncCommand
import com.projectcitybuild.features.sync.commands.SyncOtherCommand
import com.projectcitybuild.features.sync.listener.SyncRankOnJoinListener
import com.projectcitybuild.features.sync.repositories.SyncRepository
import com.projectcitybuild.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.Warp
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.integrations.DynmapIntegration
import com.projectcitybuild.integrations.EssentialsIntegration
import com.projectcitybuild.integrations.LuckPermsIntegration
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatterImpl
import com.projectcitybuild.pcbridge.core.modules.datetime.time.LocalizedTime
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import com.projectcitybuild.pcbridge.core.storage.JsonStorage
import com.projectcitybuild.pcbridge.http.HttpService
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.support.spigot.SpigotLogger
import com.projectcitybuild.support.spigot.SpigotNamespace
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

fun pluginModule(_plugin: JavaPlugin) = module {
    spigot(_plugin)
    core()
    http()
    integrations()

    // Features
    bans()
    chat()
    joinMessages()
    invisFrames()
    mute()
    nightVision()
    staffChat()
    sync()
    telemetry()
    utilities()
    warps()
}

private fun Module.spigot(plugin: JavaPlugin) {
    single { plugin }

    factory {
        get<JavaPlugin>().server
    }

    single<PlatformLogger> {
        SpigotLogger(get<JavaPlugin>().logger)
    }

    single {
        BukkitAudiences.create(get<JavaPlugin>())
    }

    single {
        SpigotNamespace(
            plugin = get(),
        )
    }

    single {
        SpigotCommandRegistry(
            plugin = get(),
            sentry = get(),
        )
    }

    single {
        SpigotListenerRegistry(
            plugin = get(),
        )
    }

    single<Permissions> {
        LuckPermsPermissions(
            logger = get(),
        )
    }
}

private fun Module.core() {
    single {
        Config(
            default = PluginConfig.default,
            jsonStorage = JsonStorage(
                file = get<JavaPlugin>().dataFolder.resolve("config.json"),
                logger = get(),
                typeToken = object : TypeToken<PluginConfig>(){},
            ),
        )
    }

    single {
        DatabaseSession(logger = get()).apply {
            val configProvider = get<Config<PluginConfig>>()
            val config = configProvider.get()
            connect(DatabaseSource.fromConfig(config))
        }
    } withOptions {
        createdAtStart()
        onClose { it?.disconnect() }
    }

    single {
        SentryReporter(
            config = get(),
            logger = get(),
        ).apply {
            val configProvider = get<Config<PluginConfig>>()
            val config = configProvider.get()
            if (config.errorReporting.isSentryEnabled) {
                start()
            }
        }
    } onClose {
        it?.close()
    }

    factory<Time> {
        val config = get<Config<PluginConfig>>().get()
        val zoneId = ZoneId.of(config.localization.timeZone)

        LocalizedTime(
            clock = Clock.system(zoneId)
        )
    }

    factory<DateTimeFormatter> {
        val config = get<Config<PluginConfig>>().get()

        DateTimeFormatterImpl(
            locale = Locale.forLanguageTag(
                config.localization.locale,
            ),
            timezone = ZoneId.of(
                config.localization.timeZone
            ),
        )
    }

    single {
        Store(
            logger = get(),
        )
    }
}

private fun Module.http() {
    single {
        val config = get<Config<PluginConfig>>().get()

        HttpService(
            authToken = config.api.token,
            baseURL = config.api.baseUrl,
            withLogging = config.api.isLoggingEnabled,
        )
    }
}

private fun Module.integrations() {
    single {
        DynmapIntegration(
            plugin = get(),
            config = get(),
            logger = get(),
            sentry = get(),
            warpRepository = get(),
        )
    }

    single {
        EssentialsIntegration(
            plugin = get(),
            logger = get(),
            sentry = get(),
        )
    }

    single {
        LuckPermsIntegration(
            logger = get(),
        )
    }
}

private fun Module.warps() {
    single {
        WarpRepository(
            db = get(),
            cache = Cache.Builder<String, Warp>()
                .expireAfterWrite(30.minutes)
                .build(),
        )
    }

    factory {
        WarpCommand(
            warpRepository = get(),
            server = get(),
        )
    }

    factory {
        WarpsCommand(
            warpRepository = get(),
            config = get(),
            server = get(),
        )
    }
}

private fun Module.joinMessages() {
    factory {
        AnnounceJoinListener(
            config = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        AnnounceQuitListener(
            config = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        FirstTimeJoinListener(
            config = get(),
            logger = get(),
            server = get(),
        )
    }

    factory {
        ServerOverviewJoinListener(
            config = get(),
        )
    }
}

private fun Module.bans() {
    factory {
        AggregateRepository(
            httpService = get<HttpService>().aggregate,
        )
    }

    factory {
        PlayerBanRepository(
            httpService = get<HttpService>().uuidBan,
        )
    }

    factory {
        PlayerUUIDRepository(
            server = get(),
            httpService = get<HttpService>().playerUuid
        )
    }

    factory {
        IPBanRepository(
            httpService = get<HttpService>().ipBan
        )
    }

    factory {
        BanCommand(
            banUUID = BanUUID(
                playerBanRepository = get(),
                playerUUIDRepository = get(),
                server = get(),
            ),
            server = get(),
        )
    }

    factory {
        BanIPCommand(
            banIP = BanIP(
                ipBanRepository = get(),
                server = get(),
            ),
            server = get(),
        )
    }

    factory {
        UnbanCommand(
            unbanUUID = UnbanUUID(
                playerBanRepository = get(),
                playerUUIDRepository = get(),
            )
        )
    }

    factory {
        UnbanIPCommand(
            unbanIP = UnbanIP(
                ipBanRepository = get(),
            )
        )
    }

    factory {
        CheckBanCommand(
            checkUUIDBan = CheckUUIDBan(
                dateTimeFormatter = get(),
                playerUUIDRepository = get(),
                playerBanRepository = get(),
            )
        )
    }

    factory {
        AuthorizeConnectionListener(
            aggregateRepository = get(),
            authoriseConnection = AuthoriseConnection(),
            dateTimeFormatter = get(),
            logger = get(),
            sentry = get(),
            server = get(),
            minecraftDispatcher = { get<JavaPlugin>().minecraftDispatcher },
            store = get(),
        )
    }
}

private fun Module.mute() {
    single(named("mute_cache")) {
        // We use a regular cache instead of the server state here
        // so that mutes persist even if the player leaves, but
        // invalidates if the plugin is reloaded/shutdown
        Cache.Builder<UUID, Unit>().build()
    }

    factory {
        MuteCommand(
            server = get(),
            mutedPlayers = get(named("mute_cache"))
        )
    }

    factory {
        UnmuteCommand(
            server = get(),
            mutedPlayers = get(named("mute_cache"))
        )
    }

    factory {
        MuteChatListener(
            mutedPlayers = get(named("mute_cache"))
        )
    }
}

private fun Module.nightVision() {
    factory {
        NightVisionCommand()
    }
}

private fun Module.invisFrames() {
    factory {
        InvisFrameCommand(
            spigotNamespace = get(),
        )
    }

    factory {
        FramePlaceListener(
            spigotNamespace = get(),
        )
    }

    factory {
        FrameItemInsertListener(
            spigotNamespace = get(),
        )
    }

    factory {
        FrameItemRemoveListener(
            spigotNamespace = get(),
        )
    }
}

private fun Module.chat() {
    factory {
        ChatBadgeRepository(
            store = get(),
            config = get(),
        )
    }

    factory {
        ChatGroupRepository(
            permissions = get(),
            config = get(),
        )
    }

    single(named("badge_cache")) {
        Cache.Builder<UUID, Component>().build()
    }

    single(named("group_cache")) {
        Cache.Builder<UUID, ChatGroupFormatter.Aggregate>().build()
    }

    factory {
        ChatBadgeFormatter(
            chatBadgeRepository = get(),
        )
    }

    factory {
        ChatGroupFormatter(
            chatGroupRepository = get(),
        )
    }

    factory {
        EmojiChatListener()
    }

    factory {
        FormatNameChatListener(
            chatGroupFormatter = get(),
            chatBadgeFormatter = get(),
            badgeCache = get(named("badge_cache")),
            groupCache = get(named("group_cache")),
        )
    }

    factory {
        SyncBadgesOnJoinListener(
            store = get(),
        )
    }
}

private fun Module.telemetry() {
    factory {
        TelemetryRepository(
            telemetryHttpService = get<HttpService>().telemetry,
        )
    }

    factory {
        TelemetryPlayerConnectListener(
            telemetryRepository = get(),
        )
    }
}

private fun Module.staffChat() {
    factory {
        StaffChatCommand(
            server = get(),
        )
    }
}

private fun Module.sync() {
    factory {
        SyncRepository(
            playerGroupHttpService = get<HttpService>().playerGroup,
            accountLinkHttpService = get<HttpService>().verificationURL,
            config = get(),
            logger = get(),
        )
    }

    factory {
        UpdatePlayerGroups(
            permissions = get(),
            syncRepository = get(),
        )
    }

    factory {
        GenerateAccountVerificationURL(
            syncRepository = get(),
        )
    }

    factory {
        SyncPlayerGroups(
            permissions = get(),
            syncRepository = get(),
        )
    }


    factory {
        SyncCommand(
            generateAccountVerificationURL = get(),
            updatePlayerGroups = get(),
        )
    }

    factory {
        SyncOtherCommand(
            server = get(),
            updatePlayerGroups = get(),
        )
    }

    factory {
        SyncRankOnJoinListener(
            syncPlayerGroups = get(),
        )
    }
}

private fun Module.utilities() {
    factory {
        PCBridgeCommand(
            plugin = get(),
        )
    }

    factory {
        PCBridgeCommand.TabCompleter()
    }
}