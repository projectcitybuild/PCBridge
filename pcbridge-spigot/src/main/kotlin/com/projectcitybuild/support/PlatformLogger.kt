package com.projectcitybuild.support

import java.util.logging.Logger

@Deprecated("Will be removed")
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
