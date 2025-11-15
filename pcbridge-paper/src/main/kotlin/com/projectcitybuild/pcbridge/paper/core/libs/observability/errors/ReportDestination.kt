package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors

interface ReportDestination {
    fun start()

    fun close()

    fun report(throwable: Throwable)
}