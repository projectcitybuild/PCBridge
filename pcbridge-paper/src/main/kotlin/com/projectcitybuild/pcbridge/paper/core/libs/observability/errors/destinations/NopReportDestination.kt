package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.destinations

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ReportDestination

class NopReportDestination: ReportDestination {
    override fun start() {}
    override fun close() {}
    override fun report(throwable: Throwable) {}
}
