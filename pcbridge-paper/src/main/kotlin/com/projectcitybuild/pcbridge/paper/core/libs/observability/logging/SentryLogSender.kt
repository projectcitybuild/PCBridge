package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.events.LogEvent
import io.klogging.rendering.itemsAndStackTrace
import io.klogging.sending.EventSender
import io.sentry.Sentry
import io.sentry.SentryLogLevel

class SentryLogSender: EventSender {
    override fun invoke(batch: List<LogEvent>) {
        val logger = Sentry.logger()

        batch.forEach { event ->
            val level = event.level.toSentryLevel() ?: return

            val metadata = if (event.level >= Level.ERROR) event.itemsAndStackTrace
                else event.items

            logger.log(level, event.message, metadata)
        }
    }
}

private fun Level.toSentryLevel(): SentryLogLevel? = when (this) {
    Level.TRACE -> SentryLogLevel.TRACE
    Level.DEBUG -> SentryLogLevel.DEBUG
    Level.INFO -> SentryLogLevel.INFO
    Level.WARN -> SentryLogLevel.WARN
    Level.ERROR -> SentryLogLevel.ERROR
    Level.FATAL -> SentryLogLevel.FATAL
    else -> null
}