package com.projectcitybuild.support.spigot

import com.projectcitybuild.support.PlatformLogger
import java.util.logging.Logger

class SpigotLogger(
    private val logger: Logger,
) : PlatformLogger {
    override val base: Logger
        get() = logger

    override fun verbose(message: String) {
        logger.info(message) // spigot doesn't log FINEST level properly
    }

    override fun debug(message: String) {
        logger.info(message) // spigot doesn't log FINE level properly
    }

    override fun info(message: String) {
        logger.info(message)
    }

    override fun warning(message: String) {
        logger.warning(message)
    }

    override fun severe(message: String) {
        logger.severe(message)
    }
}
