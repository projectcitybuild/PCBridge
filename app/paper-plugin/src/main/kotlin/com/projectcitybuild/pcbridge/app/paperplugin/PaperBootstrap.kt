package com.projectcitybuild.pcbridge.app.paperplugin

import com.projectcitybuild.pcbridge.core.observability.logging.Logger
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : PluginBootstrap {
    private lateinit var openTelemetryProvider: OpenTelemetryProvider

    override fun bootstrap(context: BootstrapContext) {
        Logger.configure(namespace = "com.projectcitybuild.pcbridge")
        openTelemetryProvider = OpenTelemetryProvider()
        TracerFactory.configure(openTelemetryProvider)
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        check(::openTelemetryProvider.isInitialized) { "Paper bootstrap has not completed" }
        return Plugin(openTelemetryProvider)
    }
}
