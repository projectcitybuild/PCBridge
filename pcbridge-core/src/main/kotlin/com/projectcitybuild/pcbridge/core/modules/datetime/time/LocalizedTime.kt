package com.projectcitybuild.pcbridge.core.modules.datetime.time

import java.time.Clock
import java.time.LocalDateTime

class LocalizedTime(
    private val clock: Clock,
): Time {
    override fun now(): LocalDateTime {
        return LocalDateTime.now(clock)
    }
}
