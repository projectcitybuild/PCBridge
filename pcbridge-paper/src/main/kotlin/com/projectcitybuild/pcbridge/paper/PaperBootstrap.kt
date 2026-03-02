package com.projectcitybuild.pcbridge.paper

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.Logger
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap: PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        Logger.configure(namespace = "com.projectcitybuild.pcbridge")
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        val otel = OpenTelemetryProvider().also {
            TracerFactory.configure(it)
        }
        val localConfig = config(context)
        val sentry = sentry(localConfig)
        val errorTracker = ErrorTracker(sentry)

        val services = Services(
            config = localConfig,
            otel = otel,
            sentry = sentry,
            errorTracker = errorTracker,
        )
        return Plugin(services)
    }

    private fun config(context: PluginProviderContext): LocalConfig {
        val storage = JsonStorage(
            typeToken = object : TypeToken<LocalConfigKeyValues>() {},
        )
        val file = context.dataDirectory.toFile().resolve("config.json")
        val config = LocalConfig(file, storage).apply {
            bootstrap()
        }
        return config
    }

    private fun sentry(localConfig: LocalConfig): SentryProvider {
        val config = localConfig.get()

        return SentryProvider(
            dsn = config.observability.sentryDsn,
            environment = config.environment.name.lowercase(),
            traceSampleRate = config.observability.traceSampleRate,
        ).apply {
            init()
        }
    }

    data class Services(
        val config: LocalConfig,
        val otel: OpenTelemetryProvider,
        val sentry: SentryProvider,
        val errorTracker: ErrorTracker,
    )
}