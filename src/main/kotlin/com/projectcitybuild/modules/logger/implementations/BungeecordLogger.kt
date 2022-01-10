package com.projectcitybuild.modules.logger.implementations

import com.projectcitybuild.modules.logger.LoggerProvider
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