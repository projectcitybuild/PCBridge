package com.projectcitybuild.pcbridge.app.paperplugin

import com.projectcitybuild.pcbridge.core.discord.DiscordSend
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.core.observability.errors.reportSafely
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
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

/**
 * Owns the runtime order: observability/config/store, registrations, integrations,
 * producers, then ingress. Shutdown reverses that order before persisting state.
 */
class PCBridgeApplication(
    private val errorTracker: ErrorTracker,
    private val sentryProvider: SentryProvider,
    private val openTelemetryProvider: OpenTelemetryProvider,
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

    @Volatile
    var state: ApplicationState = ApplicationState.NEW
        private set

    private var hydrated = false
    private var registrationsAttempted = false
    private var integrationsEnabled = false
    private var ingressStarted = false
    private var sentryInitialized = false

    suspend fun start() {
        lifecycleMutex.withLock {
            check(state == ApplicationState.NEW) { "Cannot start application in state $state" }
            state = ApplicationState.STARTING
            try {
                sentryInitialized = true
                sentryProvider.init()
                TracerFactory.configure(openTelemetryProvider)

                remoteConfig.initialize()
                store.hydrate()
                hydrated = true

                registrationsAttempted = true
                featureRegistrar.register(featureRegistrations())

                integrationRegistrar.enable(integrations())
                integrationsEnabled = true

                statsCollector.start()

                httpServer.start()
                ingressStarted = true
                state = ApplicationState.RUNNING
                httpServer.acceptIngress()
            } catch (failure: Throwable) {
                if (failure !is CancellationException) {
                    errorTracker.reportSafely(failure)
                }
                state = ApplicationState.FAILED
                rollback(failure)
                throw failure
            }
        }
    }

    suspend fun stop() {
        var cleanupFailure: Throwable? = null
        try {
            withContext(NonCancellable) {
                lifecycleMutex.withLock {
                    if (state == ApplicationState.STOPPED) return@withLock
                    state = ApplicationState.STOPPING
                    val failures = mutableListOf<Throwable>()
                    try {
                        withTimeout(30.seconds) {
                            cleanup(failures)
                        }
                    } catch (failure: Throwable) {
                        failures.add(failure)
                    } finally {
                        state = ApplicationState.STOPPED
                    }
                    throwCleanupFailures(failures)
                }
            }
        } catch (failure: Throwable) {
            cleanupFailure = failure
        }
        try {
            currentCoroutineContext().ensureActive()
        } catch (cancellation: CancellationException) {
            cleanupFailure?.let(cancellation::addSuppressed)
            throw cancellation
        }
        cleanupFailure?.let { throw it }
    }

    private suspend fun rollback(originalFailure: Throwable) {
        withContext(NonCancellable) {
            val failures = mutableListOf<Throwable>()
            try {
                withTimeout(30.seconds) {
                    cleanup(failures)
                }
            } catch (cleanupFailure: Throwable) {
                failures.add(cleanupFailure)
            }
            failures.forEach(originalFailure::addSuppressed)
            state = ApplicationState.FAILED
        }
    }

    private suspend fun cleanup(failures: MutableList<Throwable>) {
        // Phase A: quiesce ingress before anything it can call is deactivated.
        if (ingressStarted) {
            attempt(failures) {
                httpServer.stop()
                ingressStarted = false
            }
        }

        // Phase B: stop producers and wait for their owned jobs.
        attempt(failures) { statsCollector.shutdown() }
        attempt(failures) { discordSend.shutdown() }
        attempt(failures) { timer.shutdownAndJoin() }

        // Phase C: integrations depend on registrations, so unwind in reverse startup order.
        if (integrationsEnabled) {
            attempt(failures) {
                integrationRegistrar.disable()
                integrationsEnabled = false
            }
        }
        if (registrationsAttempted) {
            attempt(failures) {
                featureRegistrar.unregister()
                registrationsAttempted = false
            }
        }

        // Phase D: state mutation has stopped; flush and persist only a hydrated store.
        attempt(failures) { statsCollector.flush() }
        if (hydrated) {
            attempt(failures) {
                store.persist()
                hydrated = false
            }
        }

        // Phase E: observability is last because it reports all earlier cleanup failures.
        if (sentryInitialized) {
            attempt(failures) {
                sentryProvider.close()
                sentryInitialized = false
            }
        }
    }

    private suspend fun attempt(
        failures: MutableList<Throwable>,
        action: suspend () -> Unit,
    ) {
        try {
            withTimeout(6.seconds) { action() }
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
