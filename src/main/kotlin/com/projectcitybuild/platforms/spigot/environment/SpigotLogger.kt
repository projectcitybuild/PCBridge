package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.core.contracts.LoggerProvider
import java.util.logging.Logger

class SpigotLogger(private val logger: Logger): LoggerProvider {

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

    override fun fatal(message: String) {
        logger.severe(message)
    }
}