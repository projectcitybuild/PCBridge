package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.catching
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.paper.platform.paper.listeners.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.paper.runtime.features.PaperFeatureRegistrar
import com.projectcitybuild.pcbridge.paper.runtime.features.PaperFeatureRegistration
import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegrationRegistrar
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.runtime.state.store.Store
import com.projectcitybuild.pcbridge.webserver.HttpServer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class PluginLifecycle : KoinComponent {
    private val errorTracker: ErrorTracker by inject()
    private val listenerRegistry: SpigotListenerRegistry by inject()
    private val httpServer: HttpServer by inject()
    private val remoteConfig: RemoteConfig by inject()
    private val store: Store by inject()
    private val otel: OpenTelemetryProvider by inject()
    private val statsCollector: StatsCollector by inject()

    private val tracer by lazy { TracerFactory.make("lifecycle") }

    suspend fun boot() =
        errorTracker.catching {
            TracerFactory.configure(otel)
            tracer.trace("boot") {
                httpServer.start()
                remoteConfig.fetch()
                store.hydrate()

                get<PaperFeatureRegistrar>().register(getKoin().getAll<PaperFeatureRegistration>())

                get<PaperIntegrationRegistrar>().enable(getKoin().getAll<PaperIntegration>())

                statsCollector.start()
            }
        }

    suspend fun shutdown() =
        errorTracker.catching {
            tracer.trace("shutdown") {
                statsCollector.flush()
                statsCollector.stop()

                httpServer.stop()
                store.persist()

                get<SpigotTimer>().cancelAll()

                get<PaperIntegrationRegistrar>().disable(getKoin().getAll<PaperIntegration>())

                listenerRegistry.unregisterAll()
            }
        }
}
