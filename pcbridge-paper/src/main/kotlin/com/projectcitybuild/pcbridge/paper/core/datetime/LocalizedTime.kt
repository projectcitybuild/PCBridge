package com.projectcitybuild.pcbridge.paper.core.datetime

import java.time.Clock
import java.time.LocalDateTime

class LocalizedTime(
    private val clock: Clock,
) {
    fun now(): LocalDateTime = LocalDateTime.now(clock)
}