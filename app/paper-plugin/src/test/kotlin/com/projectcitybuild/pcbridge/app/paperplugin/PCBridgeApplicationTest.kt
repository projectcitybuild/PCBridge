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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.InOrder
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PCBridgeApplicationTest {
    @Test
    fun `startup failure is reported returned to caller and rolls back completed phases`() =
        runTest {
            val f = fixtures()
            val failure = IllegalStateException("HTTP bind failed")
            doThrow(failure).whenever(f.httpServer).start()

            val thrown = try {
                f.application.start()
                null
            } catch (failure: Throwable) {
                failure
            }

            assertSame(failure, thrown)
            assertEquals(ApplicationState.FAILED, f.application.state)
            verify(f.errorTracker).reportSafely(failure)
            verify(f.statsCollector).shutdown()
            verify(f.integrationRegistrar).disable()
            verify(f.featureRegistrar).unregister()
            verify(f.store).persist()
        }

    @Test
    fun `shutdown is phased and idempotent`() =
        runTest {
            val f = fixtures()
            f.application.start()
            f.application.stop()
            f.application.stop()

            assertEquals(ApplicationState.STOPPED, f.application.state)
            val order: InOrder = inOrder(f.httpServer, f.statsCollector, f.integrationRegistrar, f.featureRegistrar, f.store)
            order.verify(f.httpServer).stop()
            order.verify(f.statsCollector).shutdown()
            order.verify(f.integrationRegistrar).disable()
            order.verify(f.featureRegistrar).unregister()
            order.verify(f.statsCollector).flush()
            order.verify(f.store).persist()
            verify(f.httpServer).stop()
            verify(f.httpServer).acceptIngress()
            verify(f.store).persist()
        }

    @Test
    fun `registrations and integrations resolve only after config and store hydration`() =
        runTest {
            var configInitialized = false
            var storeHydrated = false
            var featuresResolved = false
            val f = fixtures(
                featureRegistrations = {
                    assertTrue(configInitialized)
                    assertTrue(storeHydrated)
                    featuresResolved = true
                    emptyList()
                },
                integrations = {
                    assertTrue(featuresResolved)
                    emptyList()
                },
            )
            doAnswer {
                configInitialized = true
                null
            }.whenever(f.remoteConfig).initialize()
            doAnswer {
                storeHydrated = true
                null
            }.whenever(f.store).hydrate()

            f.application.start()

            val order: InOrder = inOrder(
                f.remoteConfig,
                f.store,
                f.featureRegistrar,
                f.integrationRegistrar,
                f.statsCollector,
                f.httpServer,
            )
            order.verify(f.remoteConfig).initialize()
            order.verify(f.store).hydrate()
            order.verify(f.featureRegistrar).register(emptyList())
            order.verify(f.integrationRegistrar).enable(emptyList())
            order.verify(f.statsCollector).start()
            order.verify(f.httpServer).start()
            order.verify(f.httpServer).acceptIngress()
        }

    @Test
    fun `cleanup continues after server stop fails`() =
        runTest {
            val f = fixtures()
            val failure = IllegalStateException("server stop failed")
            val cleanupEvents = mutableListOf<String>()
            doAnswer {
                cleanupEvents.add("reported")
                null
            }.whenever(f.errorTracker).reportSafely(any())
            doAnswer {
                cleanupEvents.add("closed")
                null
            }.whenever(f.sentryProvider).close()
            f.application.start()
            doThrow(failure).whenever(f.httpServer).stop()

            val thrown = try {
                f.application.stop()
                null
            } catch (stopFailure: Throwable) {
                stopFailure
            }

            assertEquals(failure.message, thrown?.message)
            assertEquals(ApplicationState.STOPPED, f.application.state)
            verify(f.timer).shutdownAndJoin()
            verify(f.statsCollector).flush()
            verify(f.store).persist()
            assertEquals(listOf("reported", "closed"), cleanupEvents)
            val reportedFailure = argumentCaptor<Throwable>()
            verify(f.errorTracker).reportSafely(reportedFailure.capture())
            assertEquals(failure.message, reportedFailure.firstValue.message)
            verify(f.sentryProvider).close()
        }

    @Test
    fun `stop is safe before startup and can be repeated`() =
        runTest {
            val f = fixtures()

            f.application.stop()
            f.application.stop()

            assertEquals(ApplicationState.STOPPED, f.application.state)
            verify(f.store, org.mockito.Mockito.never()).persist()
            verify(f.timer).shutdownAndJoin()
        }

    @Test
    fun `configuration startup failure is not persisted and cancellation is preserved`() =
        runTest {
            val f = fixtures()
            val cancellation = CancellationException("startup cancelled")
            doThrow(cancellation).whenever(f.remoteConfig).initialize()

            val thrown = try {
                f.application.start()
                null
            } catch (failure: Throwable) {
                failure
            }

            assertSame(cancellation, thrown)
            assertEquals(ApplicationState.FAILED, f.application.state)
            verify(f.errorTracker, never()).reportSafely(cancellation)
            verify(f.store, never()).persist()
        }

    @Test
    fun `feature registration failure unwinds the hydrated application`() =
        runTest {
            val failure = IllegalStateException("registration failed")
            val f = fixtures(featureRegistrations = { throw failure })

            val thrown = try {
                f.application.start()
                null
            } catch (caught: Throwable) {
                caught
            }

            assertSame(failure, thrown)
            verify(f.featureRegistrar, never()).register(emptyList())
            verify(f.featureRegistrar).unregister()
            verify(f.store).persist()
            verify(f.integrationRegistrar, never()).enable(emptyList())
            verify(f.httpServer, never()).start()
        }

    @Test
    fun `background service startup failure disables activated integrations`() =
        runTest {
            val failure = IllegalStateException("stats startup failed")
            val f = fixtures()
            doThrow(failure).whenever(f.statsCollector).start()

            val thrown = try {
                f.application.start()
                null
            } catch (caught: Throwable) {
                caught
            }

            assertSame(failure, thrown)
            verify(f.integrationRegistrar).disable()
            verify(f.featureRegistrar).unregister()
            verify(f.store).persist()
            verify(f.httpServer, never()).start()
        }

    private fun fixtures(
        featureRegistrations: () -> List<PaperFeatureRegistration> = { emptyList() },
        integrations: () -> List<PaperIntegration> = { emptyList() },
    ): Fixtures {
        val errorTracker = mock<ErrorTracker>()
        val sentryProvider = mock<SentryProvider>()
        val remoteConfig = mock<RemoteConfig>()
        val store = mock<Store>()
        val featureRegistrar = mock<PaperFeatureRegistrar>()
        val integrationRegistrar = mock<PaperIntegrationRegistrar>()
        val statsCollector = mock<StatsCollector>()
        val discordSend = mock<DiscordSend>()
        val httpServer = mock<HttpServer>()
        val timer = mock<SpigotTimer>()
        val application = PCBridgeApplication(
            errorTracker = errorTracker,
            sentryProvider = sentryProvider,
            remoteConfig = remoteConfig,
            store = store,
            featureRegistrar = featureRegistrar,
            featureRegistrations = featureRegistrations,
            integrationRegistrar = integrationRegistrar,
            integrations = integrations,
            statsCollector = statsCollector,
            discordSend = discordSend,
            httpServer = httpServer,
            timer = timer,
        )
        return Fixtures(
            application,
            errorTracker,
            sentryProvider,
            remoteConfig,
            store,
            featureRegistrar,
            integrationRegistrar,
            statsCollector,
            httpServer,
            timer,
        )
    }

    private data class Fixtures(
        val application: PCBridgeApplication,
        val errorTracker: ErrorTracker,
        val sentryProvider: SentryProvider,
        val remoteConfig: RemoteConfig,
        val store: Store,
        val featureRegistrar: PaperFeatureRegistrar,
        val integrationRegistrar: PaperIntegrationRegistrar,
        val statsCollector: StatsCollector,
        val httpServer: HttpServer,
        val timer: SpigotTimer,
    )
}
