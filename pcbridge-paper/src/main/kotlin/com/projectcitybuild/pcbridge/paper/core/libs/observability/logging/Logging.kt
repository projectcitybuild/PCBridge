package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import io.klogging.noCoLogger
import io.klogging.rendering.RENDER_SIMPLE
import io.klogging.sending.STDERR
import io.klogging.sending.STDOUT

val log = Logging.instance.coLog
val noCoLog = Logging.instance.noCoLog

class Logging private constructor(namespace: String) {
    val coLog = logger(namespace)
    val noCoLog = noCoLogger(namespace)

    companion object {
        lateinit var instance: Logging

        fun configure(namespace: String) = loggingConfiguration {
            sink("stdout", RENDER_SIMPLE, STDOUT)
            sink("stderr", RENDER_SIMPLE, STDERR)

            logging {
                fromLoggerBase(namespace)
                toMaxLevel(Level.INFO) {
                    toSink("stdout")
                }
                fromMinLevel(Level.WARN) {
                    toSink("stderr")
                }
            }
            kloggingMinLogLevel(Level.DEBUG)
            minDirectLogLevel(Level.INFO)
        }.also {
            instance = Logging(namespace)
            instance.noCoLog.info { "Logger configured" }
        }
    }
}