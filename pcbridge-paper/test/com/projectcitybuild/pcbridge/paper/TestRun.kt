package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.core.observability.logging.LoggerFactory
import com.projectcitybuild.pcbridge.core.observability.tracing.OpenTelemetryProvider
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestRun : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            LoggerFactory.configure("test_logger")
            TracerFactory.configure(OpenTelemetryProvider())
        }
    }

    override fun close() {}

    companion object {
        private var started = false
    }
}