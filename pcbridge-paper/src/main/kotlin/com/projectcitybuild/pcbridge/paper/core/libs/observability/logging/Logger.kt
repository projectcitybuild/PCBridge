package com.projectcitybuild.pcbridge.paper.core.libs.observability.logging

import io.klogging.Level
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import io.klogging.noCoLogger
import io.klogging.rendering.RENDER_SIMPLE
import io.klogging.sending.STDERR
import io.klogging.sending.STDOUT

val log = logger(globalNamespace)
val noCoLog = noCoLogger(globalNamespace)

private const val globalNamespace = "com.projectcitybuild.pcbridge"

class Logging {
    companion object {
        fun configure() {
            loggingConfiguration {
                sink("stdout", RENDER_SIMPLE, STDOUT)
                sink("stderr", RENDER_SIMPLE, STDERR)

                logging {
                    fromLoggerBase(globalNamespace)
                    toMaxLevel(Level.INFO) {
                        toSink("stdout")
                    }
                    fromMinLevel(Level.WARN) {
                        toSink("stderr")
                    }
                }
                kloggingMinLogLevel(Level.DEBUG)
                minDirectLogLevel(Level.INFO)
            }
            noCoLog.info { "Logger configured" }
        }
    }
}