package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.events.LogEvent
import io.klogging.sending.EventSender
import io.sentry.Sentry
import io.sentry.SentryLevel

class SentryIssueSender: EventSender {
    override fun invoke(batch: List<LogEvent>) {
        batch.forEach { event ->
            val level = event.level.toSentryLevel() ?: return
            Sentry.captureMessage(event.message, level)
        }
    }
}

private fun Level.toSentryLevel(): SentryLevel? = when (this) {
    Level.DEBUG -> SentryLevel.DEBUG
    Level.INFO -> SentryLevel.INFO
    Level.WARN -> SentryLevel.WARNING
    Level.ERROR -> SentryLevel.ERROR
    Level.FATAL -> SentryLevel.FATAL
    else -> null
}