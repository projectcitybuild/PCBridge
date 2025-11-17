package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.Logging
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestRun : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            Logging.configure("test_logger")
        }
    }

    override fun close() {}

    companion object {
        private var started = false
    }
}