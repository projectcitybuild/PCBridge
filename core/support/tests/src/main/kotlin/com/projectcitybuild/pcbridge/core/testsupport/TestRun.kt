package com.projectcitybuild.pcbridge.core.testsupport

import com.projectcitybuild.pcbridge.core.observability.logging.Logger
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit5 extension that bootstraps shared logging/tracing infrastructure once
 * per test JVM. Auto-registered via `META-INF/services` (see
 * https://docs.junit.org/current/user-guide/#extensions-registration-automatic)
 * for any module that depends on `core:support:tests` in its test sources.
 */
class TestRun :
    BeforeAllCallback,
    ExtensionContext.Store.CloseableResource {
    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            Logger.configure("test_logger")
            TracerFactory.configure(OpenTelemetryProvider())
        }
    }

    override fun close() {}

    companion object {
        private var started = false
    }
}
