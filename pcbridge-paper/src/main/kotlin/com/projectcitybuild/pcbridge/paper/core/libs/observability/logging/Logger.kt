package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import io.klogging.noCoLogger
import io.klogging.rendering.RENDER_ANSI
import io.klogging.sending.STDERR
import io.klogging.sending.STDOUT

val log = Logger.instance.coLog
val logSync = Logger.instance.noCoLog

class Logger private constructor(namespace: String) {
    val coLog = logger(namespace)
    val noCoLog = noCoLogger(namespace)

    companion object {
        lateinit var instance: Logger

        fun configure(namespace: String) = loggingConfiguration {
            sink("stdout", RENDER_ANSI, STDOUT)
            sink("stderr", RENDER_ANSI, STDERR)
            sink("sentry-logs", SentryLogSender())
            sink("sentry-issues", SentryIssueSender())

            logging {
                fromLoggerBase(namespace)
                atLevel(Level.INFO) {
                    toSink("stdout")
                }
                fromMinLevel(Level.WARN) {
                    toSink("stderr")
                }
                fromMinLevel(Level.DEBUG) {
                    toSink("sentry-logs")
                }
                fromMinLevel(Level.ERROR) {
                    toSink("sentry-issues")
                }
            }
            // Minimum level at which log events are sent direct to sinks
            // instead of being sent asynchronously via coroutine channels
            minDirectLogLevel(Level.INFO)

            // Minimum level used by the internal logger to decide whether
            // to emit log messages. The logs emitted are diagnostic logs
            // of klogging itself (eg. "Configuration initialized")
            kloggingMinLogLevel(Level.INFO)
        }.also {
            instance = Logger(namespace)
            instance.noCoLog.info { "Logger configured" }
        }
    }
}