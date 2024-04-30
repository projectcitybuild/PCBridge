package com.projectcitybuild

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.core.database.DatabaseSource
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.data.repositories.WarpRepository
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.utilities.commands.PCBridgeCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.storage.JsonStorage
import com.projectcitybuild.support.spigot.SpigotLogger
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.koin.dsl.onClose
import kotlin.time.Duration.Companion.minutes

fun pluginModule(_plugin: JavaPlugin) = module {
    single { _plugin }

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
        WarpRepository(
            db = get(),
            cache = Cache.Builder<String, Warp>()
                .expireAfterWrite(30.minutes)
                .build(),
        )
    }

    factory {
        WarpsCommand(
            warpRepository = get(),
            audiences = get(),
            config = get(),
        )
    }
    factory {
        PCBridgeCommand(
            plugin = get(),
            audiences = get(),
        )
    }
    factory {
        PCBridgeCommand.TabCompleter()
    }
}