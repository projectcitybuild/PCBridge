package com.projectcitybuild.pcbridge.core.modules.datetime.time

import java.time.LocalDateTime

interface Time {
    fun now(): LocalDateTime
}
