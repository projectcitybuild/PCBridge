package com.projectcitybuild.pcbridge.app.paperplugin

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.catching
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.platform.paper.listeners.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistrar
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegrationRegistrar
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import com.projectcitybuild.pcbridge.platform.webserver.HttpServer
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

                get<SpigotTimer>().shutdown()

                get<PaperIntegrationRegistrar>().disable(getKoin().getAll<PaperIntegration>())

                listenerRegistry.unregisterAll()
            }
        }
}
