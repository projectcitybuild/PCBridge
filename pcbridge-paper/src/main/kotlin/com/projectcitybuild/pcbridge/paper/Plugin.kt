package com.projectcitybuild.pcbridge.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.catching
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import net.kyori.adventure.util.Services.services
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin

class Plugin(
    private val services: PaperBootstrap.Services,
): SuspendingJavaPlugin() {
    private var container: KoinApplication? = null

    override suspend fun onEnableAsync() {
        printLogo()

        val moduleList = pluginModules(this, services)
        this.container = startKoin {
            modules(moduleList)
        }
        services.errorTracker.catching {
            PluginLifecycle().boot().onFailure {
                server.pluginManager.disablePlugin(this)
            }
        }
    }

    override suspend fun onDisableAsync() {
        services.errorTracker.catching {
            PluginLifecycle().shutdown()
        }
        this.container?.close()
        this.container = null
        stopKoin()

        logger.info("Goodbye")
    }

    private fun printLogo() =
        logo
            .split("\n")
            .forEach(logger::info)
}

private val logo =
    """
    
    ██████╗  ██████╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗
    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝
    ██████╔╝██║     ██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  
    ██╔═══╝ ██║     ██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  
    ██║     ╚██████╗██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗
    ╚═╝      ╚═════╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝
    
    """.trimIndent()
