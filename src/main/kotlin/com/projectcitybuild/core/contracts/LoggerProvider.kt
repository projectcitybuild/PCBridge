package com.projectcitybuild.core.contracts

interface LoggerProvider {

    fun verbose(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun fatal(message: String)
}