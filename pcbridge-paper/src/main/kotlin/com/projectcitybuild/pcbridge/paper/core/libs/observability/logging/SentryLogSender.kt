package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.events.LogEvent
import io.klogging.rendering.itemsAndStackTrace
import io.klogging.sending.EventSender
import io.sentry.Sentry
import io.sentry.Sentry.setExtra
import io.sentry.SentryAttribute
import io.sentry.SentryAttributes
import io.sentry.SentryLogLevel
import io.sentry.logger.SentryLogParameters
import javax.management.Query.attr
import kotlin.collections.component1
import kotlin.collections.component2

class SentryLogSender: EventSender {
    override fun invoke(batch: List<LogEvent>) {
        val logger = Sentry.logger()

        batch.forEach { event ->
            val level = event.level.toSentryLevel() ?: return@forEach

            logger.log(
                level,
                SentryLogParameters.create(event.toSentryAttributes()),
                event.message,
            )
        }
    }
}

private fun LogEvent.toSentryAttributes() = SentryAttributes.fromMap(mapOf()).apply {
    val st = stackTrace
    if (st != null) {
        add(SentryAttribute.stringAttribute("stacktrace", st))
    }
    items.forEach { (key, value) ->
        add(
            when (value) {
                is String -> SentryAttribute.stringAttribute(key, value)
                is Boolean -> SentryAttribute.booleanAttribute(key, value)
                is Double -> SentryAttribute.doubleAttribute(key, value)
                is Int -> SentryAttribute.integerAttribute(key, value)
                else -> SentryAttribute.stringAttribute(key, value.toString())
            }
        )
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