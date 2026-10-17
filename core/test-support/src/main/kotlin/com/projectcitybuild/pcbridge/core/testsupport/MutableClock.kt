package com.projectcitybuild.pcbridge.core.testsupport

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MutableClock(
    var current: Instant,
) : Clock() {
    override fun getZone(): ZoneId = ZoneId.of("UTC")

    override fun withZone(zone: ZoneId): Clock = this

    override fun instant(): Instant = current
}
