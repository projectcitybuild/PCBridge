package com.projectcitybuild.pcbridge.core.contracts

interface PlatformLogger {
    fun verbose(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun severe(message: String)
}
