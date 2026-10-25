package com.projectcitybuild.pcbridge.app.paperplugin

import com.projectcitybuild.pcbridge.core.discord.DiscordSend
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.platform.webserver.HttpServer
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistrar
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegrationRegistrar
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

enum class ApplicationState {
    NEW,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED,
}

class PCBridgeApplication(
    private val errorTracker: ErrorTracker,
    private val sentryProvider: SentryProvider,
    private val remoteConfig: RemoteConfig,
    private val store: Store,
    private val featureRegistrar: PaperFeatureRegistrar,
    private val featureRegistrations: () -> List<PaperFeatureRegistration>,
    private val integrationRegistrar: PaperIntegrationRegistrar,
    private val integrations: () -> List<PaperIntegration>,
    private val statsCollector: StatsCollector,
    private val discordSend: DiscordSend,
    private val httpServer: HttpServer,
    private val timer: SpigotTimer,
) {
    private val lifecycleMutex = Mutex()

    private companion object {
        val lifecycleTimeout = 30.seconds
        val cleanupStepTimeout = 6.seconds
    }

    @Volatile
    var state: ApplicationState = ApplicationState.NEW
        private set

    private var storeHydrated = false
    private var featureRegistrationAttempted = false
    private var integrationsEnabled = false
    private var httpServerStarted = false
    private var sentryInitialized = false

    suspend fun start() = lifecycleMutex.withLock {
        check(state == ApplicationState.NEW) { "Cannot start application in state $state" }
        state = ApplicationState.STARTING
        try {
            startRuntime()
        } catch (failure: Throwable) {
            if (failure !is CancellationException) {
                errorTracker.reportSafely(failure)
            }
            state = ApplicationState.FAILED
            rollback(failure)
            throw failure
        }
    }

    suspend fun stop() {
        // Complete shutdown before propagating cancellation from the caller.
        val cleanupFailure: Throwable? = try {
            withContext(NonCancellable) {
                lifecycleMutex.withLock { stopWithLockHeld() }
            }
            null
        } catch (failure: Throwable) {
            failure
        }

        try {
            currentCoroutineContext().ensureActive()
        } catch (cancellation: CancellationException) {
            cleanupFailure?.let(cancellation::addSuppressed)
            throw cancellation
        }
        cleanupFailure?.let { throw it }
    }

    private suspend fun startRuntime() {
        sentryInitialized = true
        sentryProvider.init()

        remoteConfig.initialize()
        store.hydrate()
        storeHydrated = true

        featureRegistrationAttempted = true // Registration can fail after partial setup.
        featureRegistrar.register(featureRegistrations())

        integrationRegistrar.enable(integrations())
        integrationsEnabled = true

        statsCollector.start()

        httpServer.start()
        httpServerStarted = true
        state = ApplicationState.RUNNING
        httpServer.acceptIngress()
    }

    private suspend fun stopWithLockHeld() {
        if (state == ApplicationState.STOPPED) return

        state = ApplicationState.STOPPING
        try {
            throwCleanupFailures(cleanupWithinTimeout())
        } finally {
            state = ApplicationState.STOPPED
        }
    }

    private suspend fun rollback(originalFailure: Throwable) = withContext(NonCancellable) {
        cleanupWithinTimeout().forEach(originalFailure::addSuppressed)
        state = ApplicationState.FAILED
    }

    private suspend fun cleanupWithinTimeout(): List<Throwable> {
        val failures = mutableListOf<Throwable>()
        try {
            withTimeout(lifecycleTimeout) {
                stopHttpServer(failures)
                stopBackgroundServices(failures)
                disableIntegrations(failures)
                unregisterFeatures(failures)
                persistState(failures)
                reportCleanupFailures(failures)
                closeSentry(failures)
            }
        } catch (failure: Throwable) {
            failures.add(failure)
        }
        return failures
    }

    private suspend fun stopHttpServer(failures: MutableList<Throwable>) {
        if (!httpServerStarted) return

        attemptCleanupStep(failures) {
            httpServer.stop()
            httpServerStarted = false
        }
    }

    private suspend fun stopBackgroundServices(failures: MutableList<Throwable>) {
        attemptCleanupStep(failures) { statsCollector.shutdown() }
        attemptCleanupStep(failures) { discordSend.shutdown() }
        attemptCleanupStep(failures) { timer.shutdownAndJoin() }
    }

    private suspend fun disableIntegrations(failures: MutableList<Throwable>) {
        if (!integrationsEnabled) return

        attemptCleanupStep(failures) {
            integrationRegistrar.disable()
            integrationsEnabled = false
        }
    }

    private suspend fun unregisterFeatures(failures: MutableList<Throwable>) {
        if (!featureRegistrationAttempted) return

        attemptCleanupStep(failures) {
            featureRegistrar.unregister()
            featureRegistrationAttempted = false
        }
    }

    private suspend fun persistState(failures: MutableList<Throwable>) {
        attemptCleanupStep(failures) { statsCollector.flush() }
        if (!storeHydrated) return

        attemptCleanupStep(failures) {
            store.persist()
            storeHydrated = false
        }
    }

    private fun reportCleanupFailures(failures: List<Throwable>) {
        failures.forEach(errorTracker::reportSafely)
    }

    private suspend fun closeSentry(failures: MutableList<Throwable>) {
        if (sentryInitialized) {
            attemptCleanupStep(failures) {
                sentryProvider.close()
                sentryInitialized = false
            }
        }
    }

    private suspend fun attemptCleanupStep(
        failures: MutableList<Throwable>,
        action: suspend () -> Unit,
    ) {
        try {
            withTimeout(cleanupStepTimeout) { action() }
        } catch (failure: Throwable) {
            failures.add(failure)
        }
    }

    private fun throwCleanupFailures(failures: List<Throwable>) {
        val first = failures.firstOrNull() ?: return
        failures.drop(1).forEach(first::addSuppressed)
        throw first
    }
}
