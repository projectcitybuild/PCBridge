package com.projectcitybuild.pcbridge.app.paperplugin

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import java.util.logging.Level

class Plugin(
    private val openTelemetryProvider: OpenTelemetryProvider,
) : SuspendingJavaPlugin() {
    private var container: KoinApplication? = null
    private var application: PCBridgeApplication? = null
    private var ownsGlobalKoin = false
    private val lifecycleTracer = TracerFactory.make("app.plugin.lifecycle")

    override suspend fun onEnableAsync() {
        lifecycleTracer.trace("plugin.enable") {
            printLogo()

            try {
                val moduleList = pluginModules(this, openTelemetryProvider)
                val startedContainer = startKoin { modules(moduleList) }
                container = startedContainer
                ownsGlobalKoin = true

                val startedApplication = createPCBridgeApplication(startedContainer.koin)
                application = startedApplication
                startedApplication.start()
            } catch (failure: Throwable) {
                logger.log(Level.SEVERE, "Failed to start", failure)
                try {
                    server.pluginManager.disablePlugin(this)
                } catch (disableFailure: Throwable) {
                    failure.addSuppressed(disableFailure)
                }
                throw failure
            }
        }
    }

    override suspend fun onDisableAsync() {
        lifecycleTracer.trace("plugin.disable") {
            var failure: Throwable? = null
            try {
                shutdownPluginRuntime(
                    application = application,
                    closeContainer = { container?.close() },
                    ownsGlobalKoin = ownsGlobalKoin,
                    stopGlobalKoin = ::stopKoin,
                )
            } catch (stopFailure: Throwable) {
                failure = stopFailure
            } finally {
                application = null
                container = null
                ownsGlobalKoin = false
            }

            logger.info("Goodbye")
            failure?.let { throw it }
        }
    }

    private fun printLogo() = logo
        .split("\n")
        .forEach(logger::info)
}

internal suspend fun shutdownPluginRuntime(
    application: PCBridgeApplication?,
    closeContainer: () -> Unit,
    ownsGlobalKoin: Boolean,
    stopGlobalKoin: () -> Unit,
) {
    val failures = mutableListOf<Throwable>()
    attemptCleanup(failures) { application?.stop() }
    attemptCleanup(failures, closeContainer)
    if (ownsGlobalKoin) {
        attemptCleanup(failures, stopGlobalKoin)
    }
    throwCleanupFailures(failures)
}

private suspend fun attemptCleanup(
    failures: MutableList<Throwable>,
    action: suspend () -> Unit,
) {
    try {
        action()
    } catch (failure: Throwable) {
        failures.add(failure)
    }
}

private fun throwCleanupFailures(failures: List<Throwable>) {
    val firstFailure = failures.firstOrNull() ?: return
    failures.drop(1).forEach(firstFailure::addSuppressed)
    throw firstFailure
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
