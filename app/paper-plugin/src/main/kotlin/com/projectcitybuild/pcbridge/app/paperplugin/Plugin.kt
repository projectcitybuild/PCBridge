package com.projectcitybuild.pcbridge.app.paperplugin

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import java.util.logging.Level

class Plugin : SuspendingJavaPlugin() {
    private var container: KoinApplication? = null
    private var application: PCBridgeApplication? = null
    private var ownsGlobalKoin = false

    override suspend fun onEnableAsync() {
        printLogo()

        try {
            val moduleList = pluginModules(this)
            val startedContainer = startKoin { modules(moduleList) }
            container = startedContainer
            ownsGlobalKoin = true

            val startedApplication = createPCBridgeApplication(startedContainer.koin)
            application = startedApplication
            startedApplication.start()
        } catch (failure: Throwable) {
            logger.log(Level.SEVERE, "PCBridge failed to start", failure)
            try {
                server.pluginManager.disablePlugin(this)
            } catch (disableFailure: Throwable) {
                failure.addSuppressed(disableFailure)
            }
            throw failure
        }
    }

    override suspend fun onDisableAsync() {
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

    private fun printLogo() =
        logo
            .split("\n")
            .forEach(logger::info)

}

internal suspend fun shutdownPluginRuntime(
    application: PCBridgeApplication?,
    closeContainer: () -> Unit,
    ownsGlobalKoin: Boolean,
    stopGlobalKoin: () -> Unit,
) {
    var failure: Throwable? = null
    try {
        application?.stop()
    } catch (stopFailure: Throwable) {
        failure = stopFailure
    } finally {
        try {
            closeContainer()
        } catch (closeFailure: Throwable) {
            failure = combineFailures(failure, closeFailure)
        } finally {
            if (ownsGlobalKoin) {
                try {
                    stopGlobalKoin()
                } catch (stopKoinFailure: Throwable) {
                    failure = combineFailures(failure, stopKoinFailure)
                }
            }
        }
    }
    failure?.let { throw it }
}

private fun combineFailures(
    current: Throwable?,
    next: Throwable,
): Throwable {
    if (current == null) return next
    current.addSuppressed(next)
    return current
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
