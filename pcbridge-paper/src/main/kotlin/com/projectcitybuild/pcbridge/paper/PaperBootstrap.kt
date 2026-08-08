package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.core.observability.logging.LoggerFactory
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        LoggerFactory.configure(namespace = "com.projectcitybuild.pcbridge")
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Plugin()
    }
}