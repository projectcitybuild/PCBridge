package com.projectcitybuild.pcbridge.paper.core.libs.errors

interface ReportDestination {
    fun start()

    fun close()

    fun report(throwable: Throwable)
}