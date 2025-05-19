package com.projectcitybuild.pcbridge.paper.core.libs.errors.destinations

import com.projectcitybuild.pcbridge.paper.core.libs.errors.ReportDestination

class NopReportDestination: ReportDestination {
    override fun start() {}
    override fun close() {}
    override fun report(throwable: Throwable) {}
}
