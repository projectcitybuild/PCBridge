package com.projectcitybuild.modules.errorreporting.adapters

import com.projectcitybuild.modules.errorreporting.ErrorReporter
import io.sentry.Sentry

class SentryErrorReporter(
    private val enabled: Boolean,
    private val dsn: String
): ErrorReporter {

    override fun bootstrap() {
        if (!enabled) return

        Sentry.init { options ->
            options.dsn = dsn
        }
    }
}