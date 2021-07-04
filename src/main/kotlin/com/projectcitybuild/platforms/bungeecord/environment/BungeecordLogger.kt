package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.core.contracts.LoggerProvider
import java.util.logging.Logger

class BungeecordLogger(private val logger: Logger): LoggerProvider {

    override fun verbose(message: String) {
        logger.finest(message)
    }

    override fun debug(message: String) {
        logger.fine(message)
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