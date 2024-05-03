package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.core.database.DatabaseSource
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.core.state.Store
import com.projectcitybuild.features.chat.listeners.EmojiChatListener
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
import com.projectcitybuild.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.features.telemetry.repositories.TelemetryRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.Warp
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.integrations.DynmapIntegration
import com.projectcitybuild.integrations.EssentialsIntegration
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.datetime.time.LocalizedTime
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import com.projectcitybuild.pcbridge.core.storage.JsonStorage
import com.projectcitybuild.pcbridge.http.HttpService
import com.projectcitybuild.support.spigot.SpigotCommandRegistry
import com.projectcitybuild.support.spigot.SpigotListenerRegistry
import com.projectcitybuild.support.spigot.SpigotLogger
import com.projectcitybuild.support.spigot.SpigotNamespace
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock
import java.time.ZoneId
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

fun pluginModule(_plugin: JavaPlugin) = module {
    single { _plugin }

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

    single {
        Config(
            default = PluginConfig.default,
            jsonStorage = JsonStorage(
                file = _plugin.dataFolder.resolve("config.json"),
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
        val config = get<Config<PluginConfig>>().get()

        HttpService(
            authToken = config.api.token,
            baseURL = config.api.baseUrl,
            withLogging = config.api.isLoggingEnabled,
            contextBuilder = { _plugin.minecraftDispatcher }, // TODO
        )
    }

    factory<Time> {
        val config = get<Config<PluginConfig>>().get()
        val zoneId = ZoneId.of(config.localization.timeZone)

        LocalizedTime(
            clock = Clock.system(zoneId)
        )
    }

    single {
        SpigotNamespace(
            plugin = get(),
        )
    }

    single {
        Store(
            logger = get(),
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

    single {
        WarpRepository(
            db = get(),
            cache = Cache.Builder<String, Warp>()
                .expireAfterWrite(30.minutes)
                .build(),
        )
    }

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

    factory {
        PCBridgeCommand(
            plugin = get(),
        )
    }

    factory {
        PCBridgeCommand.TabCompleter()
    }

    factory {
        StaffChatCommand(
            server = get(),
        )
    }

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

    factory {
        EmojiChatListener()
    }

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

    factory {
        val httpService = get<HttpService>().telemetry
        val repository = TelemetryRepository(httpService)

        TelemetryPlayerConnectListener(repository)
    }

    factory { NightVisionCommand() }

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