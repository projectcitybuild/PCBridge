package com.projectcitybuild.pcbridge.core.contracts

import java.util.logging.Logger

interface PlatformLogger {
    fun verbose(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun severe(message: String)

    /**
     * Access to the underlying system [Logger]
     */
    val base: Logger
}
